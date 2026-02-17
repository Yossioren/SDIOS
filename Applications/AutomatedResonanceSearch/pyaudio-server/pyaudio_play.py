import threading

from frame_utils import (
    generate_static_frequency,
    pack_wavedata,
    play_frames,
)

SPLIT_FRAMES_TIME_SECONDS = 5


def play_frequency_once(player, *args, **kwargs):
    frames = pack_wavedata(player(*args, **kwargs))
    play_frames(frames)

def play_frequency(*args, **kwargs):
    time = kwargs["time_seconds"]
    frames = pack_wavedata(generate_static_frequency(*args, **kwargs))
    kwargs["time_seconds"] = SPLIT_FRAMES_TIME_SECONDS
    while time > 0:
        if time < SPLIT_FRAMES_TIME_SECONDS:
            kwargs["time_seconds"] = time
            frames = pack_wavedata(generate_static_frequency(*args, **kwargs))
        time -= SPLIT_FRAMES_TIME_SECONDS
        play_frames(frames)


def play_frequency_nonblocking(*args, **kwargs):
    """
    play a frequency for a fixed time!
    """
    x = threading.Thread(target=play_frequency, args=args, kwargs=kwargs)
    x.start()


def main():
    # play_frequency_nonblocking(400, 10)
    while True:
        try:
            frequency, time = input("frequency time> ").split(" ")
            frequency = float(frequency)
            time = float(time)
            play_frequency(frequency, time_seconds=time)
        except ValueError as e:
            print(e)


if __name__ == "__main__":
    main()
