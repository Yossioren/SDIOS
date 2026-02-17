import json

import matplotlib.pyplot as plt

ID = "8543213219673285"
sensor = "gyroscope"
# sensor  = 'accelerometer'
# sensor  = 'magnetometer'
activity = "game"
# activity = 'pocket'
# activity = 'rest'
# activity = 'running'
# activity = 'shaking'
# activity = 'texting'
# activity = 'walking'

where = "./measures"
path = f"{where}/{ID}/{sensor}/{activity}"
data_file = open(path, "r")
dataJ = json.loads(data_file.read())


def getJSONResetTime(varName, first):
    return lambda entryJ: entryJ[varName] - first


def getJSON(varName):
    return lambda entryJ: entryJ[varName]


def getArray(dataJ, varName):
    out = []
    for entry in dataJ:
        out.append(entry[varName])
    # print(out[0:10])
    return


print(len(dataJ))
bufTimeStamp = list(map(getJSONResetTime("t", dataJ[0]["t"]), dataJ))
bufSensor = list(map(getJSON("x"), dataJ))
plt.plot(bufTimeStamp, bufSensor, "r", label="x")
bufSensor = list(map(getJSON("y"), dataJ))
plt.plot(bufTimeStamp, bufSensor, "g", label="y")
bufSensor = list(map(getJSON("z"), dataJ))
plt.plot(bufTimeStamp, bufSensor, "b", label="z")
# plt.ylim(top=0.04)
# plt.ylim(bottom=-0.04)
plt.title(sensor + " " + activity)
plt.ylabel("sensor value")
plt.xlabel("timestamp ms")
plt.show()
