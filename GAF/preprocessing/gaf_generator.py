import gzip
import os
from typing import List

import numpy as np
from tensorflow.keras.utils import Sequence


class GAFGenerator(Sequence):
    def __init__(
        self,
        base_dir: str,
        indices: List[int],
        samples: List[str],
        axes: List[str],
        shuffle=True,
        batch_size=10,
    ):
        """
        Initializes a data generator object
          :param base_dir: the directory in which all images are stored
          :param shuffle: shuffle the data after each epoch
          :param batch_size: The size of each batch returned by __getitem__
        """
        self._base_dir = base_dir
        assert len(axes) > 0, "You should choose at least one axis"
        self._axes = axes
        self._samples = samples
        self._indices = indices
        self._size = len(self._indices)
        self._shuffle = shuffle
        self._batch_size = batch_size

    def on_epoch_end(self):
        if self._shuffle:
            np.random.shuffle(self._indices)

    def __len__(self):
        return self._size // self._batch_size

    def _load_sample(self, data_index: int, axis: str) -> np.ndarray:
        try:
            img_path = os.path.join(self._base_dir, axis, self._samples[axis][data_index])
            f = gzip.GzipFile(img_path, "r")
            return np.load(f)
        except Exception as e:
            print(f"!!! Fail {img_path} with {e}")
            return None

    def _normalize_image(
        self,
        array,
        current_range_start,
        current_range_end,
        new_range_start=0,
        new_range_end=1,
    ):
        array -= current_range_start
        array = array * ((new_range_end - new_range_start) / (current_range_end - current_range_start))
        array += new_range_start
        return array

    def __getitem__(self, idx):
        # Initializing Batch
        X = []
        start_index = idx * self._batch_size
        end_index = start_index + self._batch_size
        indices = self._indices[start_index:end_index]
        for data_index in indices:
            axes_arrays = []
            for axis in self._axes:
                loaded_sample = self._load_sample(data_index, axis)
                if loaded_sample is not None:
                    axes_arrays.append(loaded_sample)
            X.append(axes_arrays)
        if len(X) != self._batch_size:
            print(f"[-] Error loading {start_index}-{end_index}, loaded {len(X)}/{self._batch_size}")
        return self._normalize_image(np.array(X), -1, 1, 0, 1)
