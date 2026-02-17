import warnings

import matplotlib.pyplot as plt

warnings.filterwarnings("ignore")


def parseFileFloats(str_data):
    lst = str_data[1:-1].split(", ")
    out = []
    for num in lst:
        out.append(float(num))
    # print(out[0:10])
    return out


def parseFileLongs(str_data):
    lst = str_data[1:-1].split(", ")
    out = []
    for num in lst:
        out.append(int(num))
    # print(out[0:10])
    first = out[0]
    # print(list(map( lambda a : a - first, out))[0:10])
    return list(map(lambda a: a - first, out))


def plt_data(x, y, z, t, title, count=1):
    plt.figure(count)
    (x_ax,) = plt.plot(t, x, linestyle="-", color="r", label="X axis")
    (y_ax,) = plt.plot(t, y, linestyle="--", color="g", label="Y axis")
    (z_ax,) = plt.plot(t, z, linestyle="-.", color="b", label="Z axis")
    plt.legend(handles=[x_ax, y_ax, z_ax])
    plt.title(title)
    plt.ylabel("sensor value")
    plt.xlabel("timestamp ns")


def plt_file(fileX, fileY, fileZ, fileT, title, count=1):
    x = open(fileX, "r")
    y = open(fileY, "r")
    z = open(fileZ, "r")
    t = open(fileT, "r")
    bufTimeStamp = parseFileLongs(t.read())
    bufSensor = parseFileFloats(x.read())
    plt.figure(count)
    (x_ax,) = plt.plot(bufTimeStamp, bufSensor, linestyle="-", color="r", label="X axis")
    bufSensor = parseFileFloats(y.read())
    (y_ax,) = plt.plot(bufTimeStamp, bufSensor, linestyle="--", color="g", label="Y axis")
    bufSensor = parseFileFloats(z.read())
    (z_ax,) = plt.plot(bufTimeStamp, bufSensor, linestyle="-.", color="b", label="Z axis")
    plt.legend(handles=[x_ax, y_ax, z_ax])
    plt.title(title)
    plt.ylabel("sensor value")
    plt.xlabel("timestamp ns")


def plt_show():
    plt.show()
    # plt.draw()


def close_plots():
    plt.close("all")
