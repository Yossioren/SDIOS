import json
import logging
import os

from files_storage import FilesStorage
from flask import Flask, request
from models_server import ModelsServer

logging.basicConfig(filename="pacakge_server.log", encoding="utf-8", level=logging.DEBUG)
app = Flask(__name__)
SECRET_SERVER = ModelsServer(FilesStorage())


@app.route("/", methods=["GET"])
def http_default():
    return json.dumps(SECRET_SERVER.packages_info, sort_keys=True)


@app.route("/pretty", methods=["GET"])
def http_default_pretty():
    info = json.dumps(SECRET_SERVER.packages_info, indent=4, sort_keys=True)
    # return pprint.pformat(packages_info, indent=4, width=80, depth=None, compact=False, sort_dicts=True)
    return f"<pre>{info}<pre/>"


@app.route("/get_model", methods=["POST"])
def get_model():
    body_bytes = request.get_data()
    body_str = body_bytes.decode("utf-8")
    logging.debug(f"[*] Attempt to get package file {body_str}")
    [package_name, file_name] = body_str.split(",")
    data = SECRET_SERVER.get_package_file(package_name, os.path.basename(file_name))
    data_len = len(data)
    headers = {"Content-Type": "application/octet-stream", "Content-Length": data_len}
    return data, headers


@app.route("/upload", methods=["POST"])
def upload_package():
    packages = SECRET_SERVER.parse_request(request.get_json())
    logging.debug(f'[*] Request to upload packages {[package["package_name"] for package in packages]}')
    for package in packages:
        SECRET_SERVER.update_package_info(package)
        logging.debug(f'[+] upload package {package["package_name"]}')
    return "OK"


@app.route("/remove", methods=["POST"])
def remove_package():
    packages = SECRET_SERVER.parse_request(request.get_json(), False)
    logging.debug(f"[*] Request to remove packages {packages}")
    for package in packages:
        SECRET_SERVER.remove_package(package)
        logging.debug(f"[+] Remove package {package}")
    return "OK"


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=80, debug=True)
