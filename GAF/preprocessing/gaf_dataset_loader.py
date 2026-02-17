import os
from typing import List

import numpy as np

from GAF.preprocessing.gaf_generator import GAFGenerator


class GAFDatasetLoader:
    def __init__(
        self,
        base_dir: str,
        axes: List[str],
        train_test_ratio=0.7,
        shuffle=True,
        batch_size=10,
    ):
        self._base_dir = base_dir
        assert len(axes) > 0, "You should choose at least one axis"
        self._axes = axes
        self._train_test_ratio = train_test_ratio
        self._shuffle = shuffle
        self._batch_size = batch_size
        self._init_samples()
        self._init_shuffle()
        self._init_train_test()

    def _init_samples(self):
        self._samples = {}
        for axis in self._axes:
            self._samples[axis] = [f for f in os.listdir(f"{self._base_dir}/{axis}") if f and f.endswith(".npy")]
        self._total_size = len(self._samples[self._axes[0]])
        for axis in self._axes:
            assert self._total_size == len(self._samples[axis]), "All axes should have the same amount of samples"

    def _init_shuffle(self):
        self._indices = np.arange(self._total_size)
        if self._shuffle:
            np.random.shuffle(self._indices)

    def _init_train_test(self):
        mid_idx = int(self._total_size * self._train_test_ratio)
        self._train_indices = self._indices[:mid_idx]
        self._test_indices = self._indices[mid_idx:]

    def get_train(self) -> GAFGenerator:
        return GAFGenerator(
            self._base_dir,
            self._train_indices,
            self._samples,
            self._axes,
            self._shuffle,
            self._batch_size,
        )

    def get_test(self) -> GAFGenerator:
        return GAFGenerator(
            self._base_dir,
            self._test_indices,
            self._samples,
            self._axes,
            self._shuffle,
            self._batch_size,
        )
