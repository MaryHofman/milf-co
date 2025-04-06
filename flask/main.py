import os 
import json
import time
from datetime import datetime
import pytz
from flask import Flask, request, jsonify
import replicate
import speech_recognition as sr
from pydub import AudioSegment
import torch
import torchaudio
from pydub.utils import which
import numpy as np
from sklearn.cluster import KMeans
from flask import send_from_directory

# Инициализация Flask
app = Flask(__name__)

# Проверка и настройка ffmpeg
AudioSegment.ffmpeg = which("ffmpeg")
if not AudioSegment.ffmpeg:
    raise RuntimeError("FFmpeg не найден. Установите FFmpeg и добавьте в PATH")

# Настройка API Replicate
# Настройка API Replicate из переменных окружения
REPLICATE_API_TOKEN = os.getenv("REPLICATE_API_TOKEN")
if not REPLICATE_API_TOKEN:
    raise ValueError("REPLICATE_API_TOKEN не найден в .env файле")

os.environ["REPLICATE_API_TOKEN"] = REPLICATE_API_TOKEN

# Инициализация модели Silero VAD
def init_silero_vad():
    try:
        model, utils = torch.hub.load(repo_or_dir='snakers4/silero-vad',
                                      model='silero_vad',
                                      force_reload=False,
                                      onnx=False)
        get_speech_timestamps = utils[0]
        return model, get_speech_timestamps
    except Exception as e:
        print(f"⚠️ Ошибка загрузки модели Silero VAD: {str(e)}")
        return None, None

vad_model, get_speech_timestamps = init_silero_vad()

def convert_to_wav(input_path):
    """Конвертация аудио в WAV формат"""
    temp_wav = f"temp_{int(time.time())}.wav"
    try:
        audio = AudioSegment.from_file(input_path)
        audio = audio.set_frame_rate(16000).set_channels(1)  # Silero VAD требует 16kHz mono
        audio.export(temp_wav, format="wav")
        time.sleep(0.5)
        return temp_wav
    except Exception as e:
        raise RuntimeError(f"Ошибка конвертации в WAV: {str(e)}")

def analyze_speakers(audio_path):
    """Анализ аудио для идентификации спикеров с использованием Silero VAD и кластеризации"""
    if vad_model is None:
        return {}

    try:
        waveform, sample_rate = torchaudio.load(audio_path)

        # Приводим к нужному формату (16kHz mono)
        if sample_rate != 16000:
            waveform = torchaudio.functional.resample(waveform, sample_rate, 16000)
        if waveform.shape[0] > 1:
            waveform = waveform.mean(dim=0, keepdim=True)

        # Получаем сегменты с речью
        speech_timestamps = get_speech_timestamps(waveform, vad_model, threshold=0.5)

        if not speech_timestamps:
            return {}

        # Собираем признаки для кластеризации
        features = []
        for seg in speech_timestamps:
            segment = waveform[:, seg['start']:seg['end']]
            features.append([seg['start'], seg['end'], torch.mean(torch.abs(segment)).item()])

        features_array = np.array(features)[:, 2].reshape(-1, 1)
        n_clusters = min(2, len(features_array))

        if n_clusters < 2:
            kmeans = KMeans(n_clusters=1, random_state=0).fit(features_array)
        else:
            kmeans = KMeans(n_clusters=2, random_state=0).fit(features_array)

        speakers = {}
        for i, seg in enumerate(speech_timestamps):
            speaker_id = f"speaker_{kmeans.labels_[i]}"
            if speaker_id not in speakers:
                speakers[speaker_id] = []

            start_sec = round(seg['start'] / 16000, 2)
            end_sec = round(seg['end'] / 16000, 2)

            speakers[speaker_id].append({
                "start": start_sec,
                "end": end_sec,
                "text": ""
            })

        return speakers

    except Exception as e:
        print(f"⚠️ Ошибка анализа спикеров: {str(e)}")
        return {}

def recognize_speech(audio_path):
    """Распознавание речи из аудиофайла"""
    r = sr.Recognizer()
    try:
        with sr.AudioFile(audio_path) as source:
            audio = r.record(source)
            return r.recognize_google(audio, language="ru-RU")
    except Exception as e:
        raise RuntimeError(f"Ошибка распознавания речи: {str(e)}")




def summarize_with_llama(text):
    """Суммаризация текста с помощью Llama"""
    prompt = f"Создай краткое изложение на русском языке: {text}"
    try:
        output = replicate.run(
            "meta/meta-llama-3-8b-instruct",
            input={
                "prompt": prompt,
                "max_new_tokens": 150,
                "temperature": 0.4,
                "top_p": 0.9
            }
        )
        return "".join(output).strip()
    except Exception as e:
        return f"[Ошибка суммаризации: {str(e)}]"

def save_results(data):
    """Сохранение результатов в JSON"""
    filename = "result.json"
    result_data = {
        "file_url": data["source"],
        "transcript": data["full_text"],
        "summary": data["summary"],
        "speakers": data["speakers"],
        "created_at": datetime.now(pytz.timezone("Europe/Moscow")).strftime("%Y-%m-%d %H:%M:%S MSK"),
        "chat_id": None,  # Добавьте ID чата, если нужно
        "user_id": None   # Добавьте ID пользователя, если нужно
    }

    try:
        if os.path.exists(filename):
            with open(filename, "r", encoding="utf-8") as f:
                existing_data = json.load(f)
                if not isinstance(existing_data, list):
                    existing_data = []
        else:
            existing_data = []

        existing_data.append(result_data)

        with open(filename, "w", encoding="utf-8") as f:
            json.dump(existing_data, f, ensure_ascii=False, indent=2)

        print(f"\n✅ Результаты сохранены в {filename}")
    except Exception as e:
        print(f"⚠️ Ошибка сохранения результатов: {str(e)}")

def process_audio(audio_path):
    """Полная обработка аудио: спикеры + текст"""
    temp_wav = None

    try:
        # Конвертируем в WAV если нужно
        if not audio_path.lower().endswith('.wav'):
            temp_wav = convert_to_wav(audio_path)
            audio_path = temp_wav

        # Анализ спикеров
        speakers = analyze_speakers(audio_path)

        # Если нет спикеров, возвращаем пустую структуру
        if not speakers:
            return {
                "full_text": recognize_speech(audio_path),
                "speakers": {}
            }

        # Распознавание полного текста с временными метками
        r = sr.Recognizer()
        with sr.AudioFile(audio_path) as source:
            audio = r.record(source)

            # Получаем сырые данные для более точного распознавания
            try:
                # Используем Google Web Speech API с возможностью получения временных меток
                text = r.recognize_google(audio, language="ru-RU", show_all=True)

                if isinstance(text, dict) and 'alternative' in text:
                    # Берем первый вариант распознавания
                    full_text = text['alternative'][0]['transcript']




                    # Если есть временные метки слов
                    if 'words' in text['alternative'][0]:
                        words_info = text['alternative'][0]['words']
                        # Распределяем слова по спикерам
                        for speaker, segments in speakers.items():
                            for segment in segments:
                                segment_text = []
                                for word_info in words_info:
                                    word_start = float(word_info['startTime'][:-1])  # Удаляем 's' в конце
                                    word_end = float(word_info['endTime'][:-1])
                                    # Если слово попадает в сегмент спикера
                                    if (word_start >= segment['start'] and word_end <= segment['end']) or \
                                            (word_start <= segment['end'] and word_end >= segment['start']):
                                        segment_text.append(word_info['word'])
                                segment['text'] = ' '.join(segment_text)
                    else:
                        # Если нет временных меток слов, используем старый метод
                        words = full_text.split()
                        speaker_items = [item for speaker in speakers.values() for item in speaker]
                        words_per_speaker = len(words) // len(speaker_items) if speaker_items else 0
                        for i, item in enumerate(speaker_items):
                            start = i * words_per_speaker
                            end = (i + 1) * words_per_speaker if i < len(speaker_items) - 1 else len(words)
                            item['text'] = ' '.join(words[start:end])
                else:
                    # Если API вернуло просто текст
                    full_text = text
                    # Используем старый метод распределения
                    words = full_text.split()
                    speaker_items = [item for speaker in speakers.values() for item in speaker]
                    words_per_speaker = len(words) // len(speaker_items) if speaker_items else 0
                    for i, item in enumerate(speaker_items):
                        start = i * words_per_speaker
                        end = (i + 1) * words_per_speaker if i < len(speaker_items) - 1 else len(words)
                        item['text'] = ' '.join(words[start:end])

                return {
                    "full_text": full_text,
                    "speakers": speakers
                }

            except sr.UnknownValueError:
                return {"full_text": "[Речь не распознана]", "speakers": {}}
            except sr.RequestError as e:
                return {"full_text": f"[Ошибка сервиса: {e}]", "speakers": {}}

    except Exception as e:
        return {"error": str(e), "full_text": "", "speakers": {}}
    finally:
        if temp_wav and os.path.exists(temp_wav):
            try:
                os.remove(temp_wav)
            except Exception:
                pass


@app.route('/upload', methods=['POST'])
def upload_file():
    """Обработка загрузки аудиофайла и возврат результата"""
    file = request.files.get('file')
    if file:
        # Сохраняем файл
        uploads_dir = os.path.join(os.getcwd(), "uploads")
        os.makedirs(uploads_dir, exist_ok=True)
        file_path = os.path.join(uploads_dir, file.filename)
        file.save(file_path)

        try:
            # Обрабатываем файл
            result = process_audio(file_path)

            # Генерация URL для скачивания
            file_url = request.host_url.rstrip('/') + '/audio/' + file.filename
            result["source"] = file_url

            # Суммаризация и сохранение
            result["summary"] = summarize_with_llama(result["full_text"])
            save_results(result)

            return jsonify(result)
        except Exception as e:
            return jsonify({"error": str(e)}), 400

    return jsonify({"error": "No file provided"}), 400

@app.route('/results', methods=['GET'])
def get_results():
    """Возврат всех результатов в JSON формате"""
    filename = "result.json"
    if os.path.exists(filename):
        with open(filename, "r", encoding="utf-8") as f:
            results = json.load(f)
        return jsonify(results)
    else:
        return jsonify({"error": "No results found"}), 404


@app.route('/audio/<filename>', methods=['GET'])
def get_audio_file(filename):
    """Выдача аудиофайла по имени"""
    uploads_dir = os.path.join(os.getcwd(), "uploads")
    return send_from_directory(uploads_dir, filename)


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=80, debug=True)