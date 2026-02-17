import json
import os
from typing import Dict, List

from Crypto.PublicKey import RSA


class FilesStorage:
    def __init__(
        self,
        info_path: str = "info.json",
        public_key_path: str = "public_key.pem",
        classifiers_path: str = "classifiers",
    ):
        self._info_path = info_path
        self._public_key_path = public_key_path
        self._classifiers_path = classifiers_path

    def get_public_key(self) -> RSA:
        with open(self._public_key_path, "r") as f:
            return RSA.importKey(f.read())

    def get_info(self) -> List[Dict[str, any]]:
        with open(self._info_path, "r") as dataFile:
            return json.load(dataFile)

    def set_info(self, packages_info: List[Dict[str, any]]):
        with open(self._info_path, "w") as f:
            f.write(json.dumps(packages_info))

    def get_package_file(self, package: Dict[str, any], file_index: int) -> bytes:
        assert file_index in range(0, len(package["neural_networks"])), "File does not exist in the package"
        package_file_name = package["neural_networks"][file_index]["parameters"]["filename"]
        package_path = f"{self._classifiers_path}/{package['package_name']}/{package_file_name}"
        with open(package_path, "rb") as dataFile:
            return dataFile.read()

    def set_package_files(self, package: Dict[str, any]):
        for package_file in package["neural_networks"]:
            package_file = package_file["parameters"]
            package_dir = f"{self._classifiers_path}/{package['package_name']}"
            os.makedirs(package_dir, exist_ok=True)
            package_path = f"{package_dir}/{package_file['filename']}"
            with open(package_path, "wb+") as f:
                f.write(package_file["content"])

    def delete_package(self, package: Dict[str, any]):
        to_rem_dir = f'{self._classifiers_path}/{package["package_name"]}'
        list(map(os.unlink, [f"{to_rem_dir}/{f}" for f in os.listdir(to_rem_dir)]))
        os.rmdir(to_rem_dir)
