import abc

import numpy as np
import pandas as pd


class Translator(abc.ABC):
    """
    Get processed data and translate it to something else
    """

    @abc.abstractmethod
    def translate(self, data: pd.DataFrame) -> np.ndarray:
        pass
