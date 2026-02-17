import argparse
import json
import os
import time
from typing import Dict, List

import requests
from add_package import PACKAGES_ROOT, add_content_to_packages
from Crypto.Hash import SHA3_512
from Crypto.PublicKey import RSA
from Crypto.Signature import PKCS1_v1_5

SITE_ADDRESS = "http://localhost:5000"


def send_post(url: str, data: Dict):
    response = requests.post(url=url, json=data)
    print(response, response.text)


def sign(private_key: RSA.RsaKey, message: bytes) -> str:
    signer = PKCS1_v1_5.new(private_key)
    digest = SHA3_512.new()
    digest.update(message)
    signature = signer.sign(digest)
    return signature.hex()


def sign_and_send(url, packages: List[Dict], private_key: RSA.RsaKey):
    data = {"timestamp": time.time(), "packages": packages}
    data_encoded = json.dumps(data).encode("utf-8")
    signature = sign(private_key, data_encoded)
    send_post(url, {"message": data, "signature": signature})


def load_packages(packages):
    output = []
    for package in packages:
        with open(package) as f:
            output.append(json.loads(f.read()))
    return output


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--action",
        type=str,
        required=True,
        choices=["upload", "remove"],
        help="wanted action upload/remove",
    )
    parser.add_argument("--package_name", type=str, required=True, help="wanted package")
    args = parser.parse_args()
    with open("private_key.pem", "r") as f:
        private_key = RSA.importKey(f.read())
    action = args.action
    if action == "upload":
        package_path = f"{PACKAGES_ROOT}/{args.package_name}/info.json"
        assert os.path.exists(package_path), f"No config at {package_path}"
        packages_configuration = load_packages([package_path])
        sign_and_send(
            f"{SITE_ADDRESS}/upload",
            add_content_to_packages(packages_configuration),
            private_key,
        )
    elif action == "remove":
        sign_and_send(f"{SITE_ADDRESS}/remove", [args.package_name], private_key)
    else:
        print("supported actions are upload/remove")


if __name__ == "__main__":
    main()
