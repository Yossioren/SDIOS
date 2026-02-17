import gzip
import json
import math
import os
import pathlib
from typing import Any, List

import numpy as np
import pandas as pd
from numpy import ndarray

from GAF.preprocessing.data_translator.translator import Translator
from GAF.preprocessing.features_extractors.extractor import Extractor

CLASSES_ORIGIN = [
    "rest",
    "pocket",
    "walking",
    "running",
    "shaking",
    "texting",
    "game",
]  # , 'anomaly']

CWD = "."
PATH = f"{CWD}/data/processed/extracted"


class NumpyEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, ndarray):
            return obj.tolist()
        return json.JSONEncoder.default(self, obj)


class Preprocessor:
    def __init__(
        self,
        extractor: Extractor,
        translators: List[Translator] = [],
        labels=CLASSES_ORIGIN,
        train_ratio=0.70,
        packed_windows=6,
        jump_packed_rate=1,
        path=PATH,
    ):
        self._labels = labels
        self._extractor = extractor
        self._translators = translators
        self._ms_resample = self._extractor._ms_resample
        self._train_ratio = train_ratio
        self._packed_windows = packed_windows
        self._jump_packed_rate = max(jump_packed_rate, 1)
        self._path = path

    def _extract_features(self, sensor):
        x = []
        y = []
        samples_ids = []
        amount = len(os.listdir(self._path)) * len(self._labels)
        current_processed = 0
        current_missed = 0
        print(f"Scanning around {amount} records")
        for studentID in os.listdir(self._path):
            for measure_class in self._labels:
                print(
                    f"\rProgress <{current_processed}-{current_missed}/{amount}> ",
                    end="",
                )
                sample_path = "{}/{}/{}_{}.csv".format(self._path, studentID, sensor, measure_class)
                if not os.path.exists(sample_path):
                    current_missed += 1
                    continue
                current_processed += 1
                extracted = self._extract_features_file(sample_path)
                packed_windows = self._pack_groups_to_windows(extracted)
                samples_amount = extracted.shape[0]
                x.extend(packed_windows)
                y.extend(len(packed_windows) * [self._get_class(measure_class)])
                samples_ids.extend(
                    [
                        (
                            sample_path,
                            sample_index / samples_amount,
                            (sample_index + self._packed_windows) / samples_amount,
                        )
                        for sample_index in range(0, samples_amount, self._jump_packed_rate)
                    ]
                )

        print(f"Finished! - final collected <{current_processed}-{current_missed}/{amount}> ")
        data = np.array(x)
        labels = np.array(y)
        return data, labels, np.array(samples_ids)

    def _get_class(self, measure_class):
        for index in range(len(self._labels)):
            if measure_class == self._labels[index]:
                return index
        assert False

    def _pack_groups_to_windows(self, data_frame):
        # print(data_frame.shape)
        windows = []

        # total amount * features_extractors
        amount = data_frame.shape[0]
        pointer = 0

        while pointer < (amount - self._packed_windows):
            tmp_window = data_frame[pointer : pointer + self._packed_windows]
            windows.append(tmp_window)
            pointer += self._jump_packed_rate
        return windows

    def load_dataset_sensor(self, sensor):
        try:
            data, labels, samples_ids = self._load_backup_default(sensor, self._translators)
            return data, labels, samples_ids
        except FileNotFoundError:
            pass
        try:
            data, labels, samples_ids = self._load_backup_default(sensor)
        except FileNotFoundError:
            print("[*] Missing extractor cache, regenerate")
            data, labels, samples_ids = self._extract_features(sensor)
            self.backup_data(self.get_file_name(sensor), data, labels, samples_ids)
        for index in range(len(self._translators)):
            translator = self._translators[index]
            try:
                data, labels, samples_ids = self._load_backup_default(sensor, self._translators[: index + 1])
            except FileNotFoundError:
                print("[*] Missing translator cache, regenerate")
                data = translator.translate(data)
                self.backup_data(
                    self.get_file_name(sensor, self._translators[: index + 1]),
                    data,
                    labels,
                    samples_ids,
                )
        return data, labels, samples_ids

    def assert_no_none(self, *params):
        for data in params:
            assert data is not None and data is not math.nan
            if not isinstance(data, str) and hasattr(data, "__iter__"):
                if None in data:
                    print(f"None in data!\n{data}")
                    assert False
                self.assert_no_none(*data)

    def create_dataset_sensor(self, sensor):
        data, labels, ids = self.load_dataset_sensor(sensor)
        self.assert_no_none(data, labels, ids)
        return self.create_dataset(data, labels, ids)

    def create_dataset(self, x, y, info, flatten=True, shuffle=True):
        if flatten:
            nsamples, nx, ny = x.shape  # amount, window, features
            x = x.reshape((nsamples, nx * ny))
        if shuffle:
            indices = np.arange(x.shape[0])
            np.random.shuffle(indices)
            x = x[indices]
            y = y[indices]
            info = info[indices]
        return self.split_dataset_train_test(x, y, info)

    def split_dataset_train_test(self, x, y, info):
        size = int(self._train_ratio * len(x))
        x_train = x[:size]
        y_train = y[:size]
        x_test = x[size:]
        y_test = y[size:]
        info_train = info[size:]
        info_test = info[:size]
        return x_train, y_train, x_test, y_test, info_train, info_test

    def _extract_features_file(self, path: str) -> pd.DataFrame:
        data = pd.read_csv(path)
        return self._extractor.extract(data)

    def backup_data(self, file_name, data: np.array, labels: np.array, samples_ids: np.array):
        self.assert_no_none(data, labels, samples_ids)
        path = f"./{file_name}.json.gz"
        print(f"[*] Backing up preprocessing to {path}")
        os.makedirs(pathlib.Path(path).parent, exist_ok=True)
        try:
            with gzip.open(path, "wt") as f:
                print(f"[*] Encoding as json")
                backup_data = json.dumps(
                    {"data": data, "labels": labels, "samples_ids": samples_ids},
                    cls=NumpyEncoder,
                )
                print("[*] converting to bytes and compressing")
                f.write(backup_data)
                print("[+] Backed up data")
        except Exception as e:
            os.unlink(path)
            print("[-] Could not save backup! Error: ", e)

    def get_file_name(self, sensor, translators: List[Translator] = []):
        db_source = self._path.replace("\\", "/").replace("/", "_").replace("._", "")
        process_type = str(self._extractor)
        if translators:
            process_type += "_" + "_".join([str(translator) for translator in translators])
        return f"preprocessed_data/{process_type}/{db_source}_{sensor}_{self._ms_resample}x{self._packed_windows}_{len(self._labels)}"

    def _load_backup_default(self, sensor, translators: List[Translator] = []) -> tuple[ndarray, ndarray, Any]:
        return self.load_backup(self.get_file_name(sensor, translators))

    def load_backup(self, file_name) -> tuple[ndarray, ndarray, Any]:
        print("Loading db", file_name)
        file_name = f"./{file_name}.json.gz"
        with gzip.open(file_name, "rt") as f:
            print("[*] Decompressing db file")
            temp = f.read()
            print(f"[*] Parsing json")
            temp = json.loads(temp)
            print(f"[*] Converting to numpy arrays")
            data = np.array(temp["data"], dtype=np.float32)
            labels = np.array(temp["labels"])
            samples_ids = np.array(temp["samples_ids"])
            self.assert_no_none(data, labels, samples_ids)
            return data, labels, samples_ids

    def __str__(self):
        process_type = str(self._extractor)
        if self._translators:
            process_type += "_" + "_".join([str(translator) for translator in self._translators])
        return process_type
