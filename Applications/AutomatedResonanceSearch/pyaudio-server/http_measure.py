import os
from datetime import datetime

import pandas as pd
import uvicorn
from fastapi import FastAPI, HTTPException, Request, Response
from pyaudio_play import play_frequency_nonblocking
from pydantic import BaseModel

app = FastAPI()


@app.post("/make_frequency")
async def make_frequency(request: Request):
    data = await request.body()
    print(data)
    frequency, time = data.decode().split(" ")
    play_frequency_nonblocking(float(frequency), time_seconds=float(time) / 1000.0)
    return Response(content="Ok", media_type="application/text")


class Result(BaseModel):
    sensor_name: str
    build_model: str
    frequency: float
    entry: dict


@app.post("/save_results")
def save_results(result: Result):
    if "." in result.sensor_name + result.build_model or ".." in str(result.frequency):
        raise HTTPException(status_code=403, detail="Fail")
    date_format = datetime.utcnow().strftime("%d.%m")
    path = "measurements_directory/{}/{}/{}/{}".format(
        result.sensor_name, result.build_model, date_format, result.frequency
    )
    hourformat = datetime.utcnow().strftime("%H:%M")
    file_path = "{}/{}.csv".format(path, hourformat)
    print(f"Saving results to {file_path}")
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    df = pd.DataFrame()
    df["x"] = result.entry["x"]
    df["y"] = result.entry["y"]
    df["z"] = result.entry["z"]
    df["t"] = result.entry["t"]
    df.to_csv(file_path, encoding="utf-8")
    return Response(content="Ok", media_type="application/text")


@app.get("/")
def hello_world():
    return Response(content="Hello World", media_type="application/text")


# uvicorn http_measure:app --workers 4
# uvicorn http_measure:app --port 8000 --host 0.0.0.0 --reload
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port="8000")
