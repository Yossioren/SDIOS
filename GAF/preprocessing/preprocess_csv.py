import csv
import os
from typing import List

import pandas as pd

# get the full recording save parts and move with offset
# record has many samples - for sample size S, jump size J=S/OFFSET <0-S>,<J,S+J>
OFFSET = 3
SAMPLE_SIZE_MODIFIER = 2
REWRITE_PROCESSED_FILES = True
USE_ADDITIONAL_DB = True
# find a good method to make each frame in constant size
# save without time
#
# Fix time to fixed points
# lets try to train for 60hz measures
# calculate point p1<-x---->p2; dp1=distance(x,p1); dp2=distance(x,p2); dtot=dp1+dp2; x=p1*dp1/dtot + p2*dp2/dtot
#
# Files output
# studentID
# -<sensor>_<action>_origin.csv - full
# -<sensor>_<action>_processed.csv - fixed point time
# -<sensor>_<action>/<num>.csv - multiplied fixed


CLASSES = [
    "game",
    "pocket",
    "rest",
    "running",
    "shaking",
    "texting",
    "walking",
    "anomaly",
]
SENSORS = ["gyroscope", "accelerometer", "magnetometer"]
SENSORS_HZ = {"gyroscope": 120, "accelerometer": 120, "magnetometer": 60}

# BENIGN INPUT
print(SENSORS_HZ)
print(os.getcwd())
CWD = "."
INPUT_DIR = f"{CWD}/data/origin/data"
ADDITIONAL_INPUT_DIR = f"{CWD}/data/extra measures"
EXTRACTED_DIR = f"{CWD}/data/processed/extracted"
FIXED_OUTPUT_DIR = f"{CWD}/data/processed/fixed_{{}}"
METADATA_FILE = f"{CWD}/data/processed/metadata_{{}}.csv"
ADDITIONAL_METADATA_FILE = f"{CWD}/data/processed/additional_metadata_{{}}.csv"
CLEAN_ORIRGIN_SOURCE = f"{CWD}/data/clean origin/merged_{{}}_students.csv"

# ANOMALIES
INPUT_ANOMALIES = "anomalies/anomalies.csv"
ADDITIONAL_INPUT_ANOMALIES = f"{CWD}/anomalies/original"
ANOMALIES_EXTRACTED_DIR = f"{CWD}/anomalies/processed/extracted"
ANOMALIES_FIXED_OUTPUT_DIR = f"{CWD}/anomalies/processed/fixed_{{}}"


def main():
    handle_normal_records()
    handle_anomaly_records()


def handle_normal_records():
    for sensor in SENSORS:
        extract_metadata(CLEAN_ORIRGIN_SOURCE.format(sensor), METADATA_FILE.format(sensor))
        extract_metadata(
            f"{ADDITIONAL_INPUT_DIR}/{sensor}.csv",
            ADDITIONAL_METADATA_FILE.format(sensor),
        )
    extract_measures(INPUT_DIR)
    if USE_ADDITIONAL_DB:
        extract_extra_measures(ADDITIONAL_INPUT_DIR)
    fix_time_data(EXTRACTED_DIR, FIXED_OUTPUT_DIR)


def handle_anomaly_records():
    extract_anomalies_csv(INPUT_ANOMALIES, "gyroscope", ANOMALIES_EXTRACTED_DIR)
    extract_extra_anomalies(ADDITIONAL_INPUT_ANOMALIES, ANOMALIES_EXTRACTED_DIR)
    fix_time_data(ANOMALIES_EXTRACTED_DIR, ANOMALIES_FIXED_OUTPUT_DIR)


def fix_time_data(extracted_dir, fixed_output_dir):
    """
    -x--c---y
    calculate diff y-c c-x use x and y with relative to the diff - the one that is closer is the effective
    """
    print("fix_time_data")
    amount = len(os.listdir(extracted_dir)) * len(CLASSES) * len(SENSORS)
    current_processed = 0
    current_missed = 0
    print(f"Scanning around {amount} records")
    for studentID in os.listdir(extracted_dir):
        for sensor in SENSORS:
            sensor_output = fixed_output_dir.format(SENSORS_HZ[sensor])
            for measure_class in CLASSES:
                print(
                    f"\rProgress <{current_processed}-{current_missed}/{amount}> ",
                    end="",
                )
                path = "{}/{}/{}_{}.csv".format(extracted_dir, studentID, sensor, measure_class)
                if not os.path.exists(path):
                    current_missed += 1
                    continue
                current_processed += 1
                processed_path = "{}/{}/{}_{}.csv".format(sensor_output, studentID, sensor, measure_class)
                if (not REWRITE_PROCESSED_FILES) and os.path.exists(processed_path):
                    continue
                data = pd.read_csv(path)
                data["t"] = data["t"].astype("datetime64[ns]")
                last_index = len(data) - 1
                sensor_measure_time = 1 / SENSORS_HZ[sensor]
                os.makedirs(os.path.dirname(processed_path), exist_ok=True)
                with open(processed_path, "w+") as f:
                    csv_writer = csv.writer(
                        f,
                        delimiter=",",
                    )
                    csv_writer.writerow(["x", "y", "z", "t"])
                    start_time = data["t"][0].timestamp()
                    end_time = data["t"][last_index].timestamp() - start_time
                    current_time = 0
                    data_index = 0
                    while current_time < end_time:
                        current_time_sample = start_time + current_time
                        while data_index < last_index and current_time_sample >= data["t"][data_index + 1].timestamp():
                            data_index += 1
                        if data_index + 1 >= last_index:
                            break  # does this filled it correcly?
                        dptot = data["t"][data_index + 1].timestamp() - data["t"][data_index].timestamp()
                        dp1 = (current_time_sample - data["t"][data_index].timestamp()) / dptot
                        dp2 = 1 - dp1
                        x = data["x"][data_index] * dp2 + data["x"][data_index + 1] * dp1
                        y = data["y"][data_index] * dp2 + data["y"][data_index + 1] * dp1
                        z = data["z"][data_index] * dp2 + data["z"][data_index + 1] * dp1
                        csv_writer.writerow([x, y, z, current_time])
                        current_time += sensor_measure_time


def indexOf(lst, value):
    try:
        return lst.index(value)
    except ValueError:
        return -1


def read_dataset_file(f, sensor=None, label=None, output_dir=EXTRACTED_DIR):
    measures_reader = csv.reader(f, delimiter=",").__iter__()
    csvheader = next(measures_reader)
    id_index = indexOf(csvheader, "studentID")
    if id_index == -1:
        id_index = indexOf(csvheader, "ID")
    sensor_index = indexOf(csvheader, "sensor")
    label_index = indexOf(csvheader, "activity")
    assert id_index != -1, "Cannot determine id index"
    assert sensor_index != -1 or sensor is not None, "Cannot determine sensor"
    assert label_index != -1 or label is not None, "Cannot determine label"

    studentID = None
    writer = None
    csv_writer = None
    for row in measures_reader:
        if row[id_index] == "studentID":
            studentID = None
            if writer:
                writer.close()
            continue

        if (
            studentID != row[id_index]
            or (label_index != -1 and label != row[label_index])
            or (sensor_index != -1 and sensor != row[sensor_index])
        ):
            studentID = row[id_index]
            if sensor_index != -1:
                sensor = row[sensor_index]
            if label_index != -1:
                label = row[label_index]
            # print(studentID, sensor, label)
            file_path = "{}/{}/{}_{}.csv".format(output_dir, studentID, sensor, label)
            os.makedirs(os.path.dirname(file_path), exist_ok=True)
            writer = open(file_path, "w+")
            csv_writer = csv.writer(
                writer,
                delimiter=",",
            )
            csv_writer.writerow(["x", "y", "z", "t"])
        csv_writer.writerow(row[1:id_index])


def extract_extra_anomalies(input_dir, output_dir):
    print("extracting", input_dir)
    for directory, _directories, files in os.walk(input_dir):
        for sub_file in files:
            if not sub_file.endswith(".csv"):
                continue
            try:
                input_path = f"{directory}/{sub_file}"
                file_id = input_path.replace(input_dir, "").replace("\\", "/").replace("/", "_")
                parse_sub_file(file_id, input_path, output_dir)
            except Exception as e:
                print(e)


def parse_sub_file(file_id, input_path, output_dir):
    data = pd.read_csv(input_path)
    if "x" not in data:
        data = pd.read_csv(input_path, sep="\t")
    sensor_name = "unknown"
    for sensor in SENSORS:
        if sensor in input_path.lower():
            sensor_name = sensor
    output_path = "{}/{}/{}_{}.csv".format(output_dir, file_id, sensor_name, "anomaly")
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    data = data[["x", "y", "z", "t"]]
    data.to_csv(output_path)


def extract_anomalies_csv(input_path, sensor, output_dir):
    print("extracting", input_path)
    data = pd.read_csv(input_path)
    for group_id, group_df in data.groupby(["freq", "phone"]):
        file_path = "{}/{}/{}_{}.csv".format(output_dir, group_id, sensor, "anomaly")
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        group_df = group_df[["x", "y", "z", "t"]]
        group_df.to_csv(file_path)


# processed for each student a recording of sensor and action consists of xyzt
def extract_measures(input_dir):
    print("extracting", input_dir)
    for measure_class in CLASSES:
        for sensor in SENSORS:
            path = "{}/{}_{}.csv".format(input_dir, sensor, measure_class)
            if not os.path.exists(path):
                print(f"Missing path {path}")
                continue
            with open(path, "r") as f:
                read_dataset_file(f, sensor, measure_class)


def extract_extra_measures(input_dir):
    print("extracting", input_dir)
    for sensor in SENSORS:
        with open("{}/{}.csv".format(input_dir, sensor), "r") as f:
            read_dataset_file(f)


def extract_metadata(
    path: str,
    output_path: str,
    labels: List[str] = [
        "x",
        "y",
        "z",
        "timestamp_ms",
        "t",
        "Unnamed: 0",
        "Unnamed: 0.1",
    ],
):
    df = pd.read_csv(path)
    for label in labels:
        try:
            df = df.drop(label, axis=1)
        except KeyError:
            print(f"{path} does not contain {label}")
    df = df.drop_duplicates()
    df.to_csv(output_path, index=None)


if __name__ == "__main__":
    main()
