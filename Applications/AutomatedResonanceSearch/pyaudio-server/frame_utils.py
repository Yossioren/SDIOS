import math
import struct

import pyaudio

FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 44100

p = pyaudio.PyAudio()
TWO_PI = 2 * math.pi


def play_frames(frames: bytes):
    if not frames:
        return
    stream = p.open(format=FORMAT, channels=CHANNELS, rate=RATE, output=True)
    stream.write(frames)
    stream.stop_stream()
    stream.close()


def generate_static_frequency(frequency: float, time_seconds: float):
    """get frames for a fixed frequency for a specified time or
    number of frames, if frame_count is specified, the specified
    time is ignored"""
    print(f"generating audio for standing frequency {frequency} {time_seconds}")
    frame_count = int(RATE * time_seconds)

    remainder_frames = frame_count % RATE
    wavedata = []

    a = RATE / frequency  # number of frames per wave
    b_inc = 1 / a
    b = 0
    for _ in range(frame_count):
        b += b_inc
        c = b * TWO_PI
        d = math.sin(c) * 32767
        e = int(d)
        wavedata.append(e)

    for i in range(remainder_frames):
        wavedata.append(0)

    return wavedata


AH = 1.0
AL = 0.2


def pack_wavedata(wavedata):
    number_of_bytes = str(len(wavedata))
    wavedata = struct.pack(number_of_bytes + "h", *wavedata)
    return wavedata


def show():
    wavedata = generate_static_frequency(19323.75, 1)
    import matplotlib.pyplot as plt
    plt.figure(1)
    plt.plot(range(len(wavedata)), wavedata, linestyle="--", color="b")
    plt.title("wavem data")
    plt.ylabel("Amplitude")
    plt.xlabel("Frame count")
    plt.show()


if __name__ == "__main__":
    show()
