from pyaudio_play import play_frequency


def main():
    start = 23600
    stop = 25000
    jump = 25
    dwell = 0.5
    for frequency in range(start, stop, jump):
        print(frequency)
        play_frequency(float(frequency), time_seconds=dwell)


if __name__ == "__main__":
    main()
