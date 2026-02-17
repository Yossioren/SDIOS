import pandas as pd

from GAF.preprocessing.consts import AXES
from GAF.preprocessing.features_extractors.extractor import Extractor


class RawExtractor(Extractor):
    def __init__(self, resample_amount="500"):
        super().__init__(resample_amount)

    def extract(self, data: pd.DataFrame) -> pd.DataFrame:
        data["tot"] = pow((data[AXES] * data[AXES]).sum(axis=1), 0.5)
        data = data.set_index(["t"])
        data.index = pd.to_datetime(data.index, unit="s")

        resample = data.resample(self._ms_resample)

        extracted_data_by_resample = [time_window for time_window in self._iterate_by_resample(data, resample)]
        output = pd.DataFrame()
        for axis in AXES + ["tot"]:
            output[axis] = [time_window[axis].to_list() for time_window in extracted_data_by_resample]
        output = output.dropna()
        return output

    def __str__(self):
        return "RawExtractor"
