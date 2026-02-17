import json
import os

from flask import Flask, request
from flask_api import status

stop_msg = "stop"
host = "0.0.0.0"
port = 80  # 443
parent_dir = "measures"
app = Flask(__name__)


@app.route("/")
def hello():
    return "Hello World!"


@app.route("/secret")
def secret():
    return "YEPY HEY DO!!"


@app.route("/", methods=["POST"])
def save_data():
    req_data = request.data
    req_json = json.loads(req_data.decode("utf-8"))
    # print(req_json)
    if not ("path" in req_json and "data" in req_json and "filename" in req_json):
        return "Body does not have all fields", status.HTTP_400_BAD_REQUEST
    path = req_json["path"]
    filename = req_json["filename"]
    data = req_json["data"]
    path = f"{parent_dir}/{path}"
    fullpath = f"{path}/{filename}"
    if ".." in fullpath:  # directory traversal!
        return "'..' are forbidden", status.HTTP_403_FORBIDDEN
    os.makedirs(path, exist_ok=True)
    f = open(fullpath, "x")
    f.write(data)
    return ""


if __name__ == "__main__":
    # app.run(host=host, port=port, ssl_context=('cert.pem', 'key.pem'), debug=True)
    # app.run(host=host, port=port, ssl_context='adhoc', debug=True)
    app.run(host=host, port=port, debug=True)
