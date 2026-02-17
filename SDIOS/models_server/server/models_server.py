import base64
import json
import logging
import time
from typing import Dict, List, Tuple

from Crypto.Hash import SHA3_512
from Crypto.Signature import PKCS1_v1_5
from files_storage import FilesStorage
from werkzeug.utils import secure_filename


class ModelsServer:
    TIMESTAMP_DIFF = 600  # ten minutes # unix time should be consistent
    ALLOWED_EXTENSIONS = {"tflite"}

    def __init__(self, files_storage: FilesStorage):
        self._packages_info = []
        self._name_to_package = {}
        self._files_storage = files_storage
        self._public_key = self._files_storage.get_public_key()
        self._packages_info = self._files_storage.get_info()
        self._name_to_package = {package["package_name"]: package for package in self._packages_info}

    @property
    def packages_info(self) -> List:
        return self._packages_info

    def _validate_secret(self, signed_message: Dict):
        message = signed_message["message"]
        message_bytes = json.dumps(signed_message["message"]).encode("utf-8")
        signature = signed_message["signature"]
        digest = SHA3_512.new()
        digest.update(message_bytes)
        signature_bytes = bytes.fromhex(signature)
        assert PKCS1_v1_5.new(self._public_key).verify(digest, signature_bytes), "Message not verified"
        timestamp = message["timestamp"]
        diff = abs(time.time() - timestamp)
        assert diff < self.TIMESTAMP_DIFF, "Message timestamp has to be recently"
        logging.debug("[+] Passed root authentication")

    def parse_request(self, signed_message: Dict, validate_params: bool = True) -> Tuple[Dict, List]:
        self._validate_secret(signed_message)
        message = signed_message["message"]
        if validate_params:
            self._validate_package(message)
        return message["packages"]

    def _validate_extension(self, filename: str):
        return "." in filename and filename.rsplit(".", 1)[1].lower() in self.ALLOWED_EXTENSIONS

    def _validate_package(self, packages: Dict):
        for package in packages["packages"]:
            assert "package_name" in package and "version" in package and "description" in package
            if "neural_networks" in package:
                for i in range(len(package["neural_networks"])):
                    package_file = package["neural_networks"][i]["parameters"]
                    filename = package_file["filename"]
                    assert self._validate_extension(filename)
                    package_file["filename"] = secure_filename(filename)
                    package_file["content"] = base64.b85decode(package_file["content"])

    def update_package_info(self, package):
        if package["package_name"] in self._name_to_package:
            old_package = self._name_to_package[package["package_name"]]
            self._packages_info.remove(old_package)
        self._files_storage.set_package_files(package)
        [package_file["parameters"].pop("content") for package_file in package.get("neural_networks", {})]
        self._packages_info.append(package)
        self._name_to_package[package["package_name"]] = package
        self._files_storage.set_info(self._packages_info)

    def remove_package(self, package):
        assert package
        package = self._name_to_package.pop(package)
        self._packages_info.remove(package)
        self._files_storage.set_info(self._packages_info)
        self._files_storage.delete_package(package)

    def get_package_file(self, package_name: str, file_name: str) -> bytes:
        assert package_name in self._name_to_package
        package = self._name_to_package[package_name]
        for file_index in range(len(package["neural_networks"])):
            if package["neural_networks"][file_index]["parameters"]["filename"] == file_name:
                return self._files_storage.get_package_file(package, file_index)
        assert False
