# Trusted model server
This directory implements the trusted server that can retrieve the SDIOS package for your phone. The SDIOS package contains the TensorFlow-Lite models, preprocessing instructions, default values, and user-configurable parameters.

The directory ['model_controller'](./model_controller/) contains scripts to update and remove the suggested models and an example SDIOS package.

Start all scripts from their directories.

```bash
cd SDIOS/models_server
./setup.sh
cd server
./start.sh <http-server-port>
```
