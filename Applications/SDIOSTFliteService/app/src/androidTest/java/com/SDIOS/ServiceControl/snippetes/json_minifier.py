import json


def minify(input: str) -> str:
    return json.dumps(json.loads(input))


x = """"""
minify(x)
