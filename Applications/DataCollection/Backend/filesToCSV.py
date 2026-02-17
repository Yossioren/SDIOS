import csv
import json
from os import listdir
from os.path import isdir

where = "./measures"
phonemodelfilename = "phoneModel.json"
outputFile = f"{where}/out.csv"


def writeOnCSVData(str_data):
    dataJ = json.loads(str_data)
    with open(outputFile, "a", newline="") as csvfile:
        writer = csv.writer(csvfile, delimiter=",", quotechar="|", quoting=csv.QUOTE_MINIMAL)
        for entry in dataJ:
            writer.writerow([entry["x"], entry["y"], entry["z"], entry["t"]])


def parseFileLongs(str_data):
    lst = str_data[1:-1].split(", ")
    out = []
    for num in lst:
        out.append(int(num))
    # print(out[0:10])
    first = out[0]
    # print(list(map( lambda a : a - first, out))[0:10])
    return list(map(lambda a: a - first, out))


def writeOnCSVRow(lst, mode="a"):
    with open(outputFile, mode, newline="") as csvfile:
        writer = csv.writer(csvfile, delimiter=",", quotechar="|", quoting=csv.QUOTE_MINIMAL)
        writer.writerow(lst)


writeOnCSVRow(["x", "y", "z", "timestamp_ms", "or", "!", "ID", "sensor", "activity", "JSON"], "w")
mypath = f"{where}"

starsRoll = ["|", "/", "-", "\\"]
starsArrLen = len(starsRoll)


def porgressBar(lenBar, cur, max):
    stars = (lenBar * cur) // max
    bar = f'[{"*"*stars}{starsRoll[cur%starsArrLen]}{" "*(lenBar-1-stars)}]'
    return f"{cur}/{max} {bar}"


students = [f for f in listdir(mypath) if isdir(f"{where}/{f}")]
count = 0
for student in students:
    count += 1
    print(f"student {porgressBar(15, count, len(students))}", end="\r")
    mypath = f"{where}/{student}"
    sensors = [f for f in listdir(mypath) if isdir(f"{where}/{student}/{f}")]
    with open(f"{where}/{student}/{phonemodelfilename}", "r") as dataFile:
        phonemodeltext = dataFile.read()
    for sensor in sensors:
        mypath = f"{where}/{student}/{sensor}"
        activities = [f for f in listdir(mypath)]
        outputList = []
        for activity in activities:
            mypath = f"{where}/{student}/{sensor}/{activity}"
            # writeOnCSV(['','','','', student, sensor, activity])
            writeOnCSVRow(["!", student, sensor, activity, phonemodeltext])
            with open(mypath, "r") as dataFile:
                writeOnCSVData(dataFile.read())
