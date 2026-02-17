import abc

from pandas import DataFrame


class Extractor(abc.ABC):
    """
    Gets a full sample of one activity recorded on one of the phone's sensor
    The length of the recording is about ~60 seconds
    However sampling rate may vary and we will get different length of recording
    for different smartphones
    """

    def __init__(self, resample_amount="500", resample_units="ms"):
        self._resample_amount = resample_amount
        self._resample_units = resample_units
        self._ms_resample = f"{resample_amount}{resample_units}"

    @abc.abstractmethod
    def extract(self, data: DataFrame):
        pass

    @staticmethod
    def _iterate_by_resample(data, resample):
        dates = list(resample.groups.keys())
        previous_timestamp = dates[0]
        for i in range(1, len(dates)):
            current_timestamp = dates[i]
            out = data.loc[(data.index <= current_timestamp) & (data.index >= previous_timestamp)]
            yield out.sort_values(by=["t"])
            previous_timestamp = current_timestamp
        yield data.loc[(data.index >= previous_timestamp)]
