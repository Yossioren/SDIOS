from typing import Iterator

import numpy as np
import pandas as pd
from pyts.image import GramianAngularField

from GAF.preprocessing.consts import AXES_TOT as AXES
from GAF.preprocessing.data_translator.translator import Translator


class GafTranslator(Translator):
    def __init__(self, axis_windows_amount: int, windows: int, method: str = "summation"):
        """
        method can be summation/difference
        input series size have to be fixed because image size is related to the series size
        """
        self._axis_windows_amount = axis_windows_amount
        self._windows = windows
        self._method = method

    def _get_gaf(self, data):
        return np.array(GramianAngularField(method=self._method).fit_transform(data))

    def _get_axis(self, data, axis, headers=AXES):
        axis_index = headers.index(axis)
        return [entry[axis_index] for entry in data]

    def _flatten_data(self, data, headers=AXES, used_headers=AXES):
        for sample in data:
            merged_windows = []
            assert len(sample.shape) == 2 and sample.shape[0] == self._windows, "Dimension validate"
            for header in used_headers:
                header_index = headers.index(header)
                sub_arr = []
                for window in range(0, self._windows):
                    sub_arr.extend(sample[window][header_index])
                tmp = np.resize(np.array(sub_arr), self._axis_windows_amount)
                merged_windows.append(tmp)
            yield merged_windows

    def translate(self, samples: pd.DataFrame) -> np.ndarray:
        return np.fromiter(self._translate_helper(samples), dtype=list)

    def translate_axis_yield(self, samples: pd.DataFrame, axis) -> Iterator[np.ndarray]:
        for gaf_image in self._translate_axis(samples, AXES, axis):
            # print("flattened_data", gaf_image.shape)
            yield gaf_image[0]

    def _translate_helper(self, samples: pd.DataFrame) -> Iterator[np.ndarray]:
        for axis in AXES:
            flattened_data = np.array(list(self._translate_axis(samples, AXES, axis)))
            print("flattened_data", flattened_data.shape)
            nsamples, _1, nx, ny = flattened_data.shape  # amount, 1, gaf_x, gaf_y
            yield flattened_data.reshape((nsamples, nx * ny))

    def _translate_axis(self, samples: pd.DataFrame, axes, axis):
        for sample in self._flatten_data(samples, axes, [axis]):
            yield self._get_gaf(sample)

    def __str__(self):
        return f"GafTranslator_{self._method}"
