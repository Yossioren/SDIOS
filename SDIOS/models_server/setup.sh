#!/usr/bin/env bash

python3 -m pip install -U pip setuptools wheel
python3 -m venv .venv
./.venv/Scripts/pip install -r requirements-full.txt
./.venv/Scripts/python ./create_key_pair.py
