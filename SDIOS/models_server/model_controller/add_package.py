import base64
from typing import Dict, List

PACKAGES_ROOT = "packages"
classifiers_path = "classifiers"


def convert_h5_to_tflite(package_file: Dict, package_file_path: str):
    """
    Import tensorflow in the function
    because the import takes few seconds and should be triggered just on h5 files which are not common
    """
    import tensorflow

    loaded_model = tensorflow.keras.models.load_model(package_file_path)
    converter = tensorflow.lite.TFLiteConverter.from_keras_model(loaded_model)
    package_file["filename"] = package_file["filename"].replace(".h5", ".tflite")
    package_file["content"] = base64.b85encode(converter.convert()).decode()


def load_file_content(package_file: Dict, package_file_path: str):
    if package_file["filename"].endswith(".h5"):
        convert_h5_to_tflite(package_file, package_file_path)
    elif package_file["filename"].endswith(".tflite"):
        with open(package_file_path, "rb") as f:
            package_file["content"] = base64.b85encode(f.read()).decode()
    else:
        assert False, f"Unsupported filetype! {package_file['filename']}"


def add_content_to_packages(packages: List[Dict[str, any]]) -> List[Dict]:
    for package in packages:
        for package_file in package["neural_networks"]:
            package_file = package_file["parameters"]
            package_file_path = f'{PACKAGES_ROOT}/{package["package_name"]}/{package_file["filename"]}'
            load_file_content(package_file, package_file_path)
    return packages
