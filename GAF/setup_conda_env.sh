export CONDA_ALWAYS_YES="true"
CONDA_NAME="train_tf"

# Remove old conda environment
conda remove -n $CONDA_NAME -y --all
jupyter kernelspec uninstall $CONDA_NAME

# Create conda_env
conda init bash
conda create -n $CONDA_NAME python=3.10 -y
conda activate $CONDA_NAME

python -m ipykernel install --user --name $CONDA_NAME --display-name "TF training 3.10 ($CONDA_NAME)"

# Show conda info
conda info
conda env list

# Install conda requirements
# You may need to adapt the installation to your GPU
conda install -c conda-forge cudatoolkit==11.8.* cxx-compiler -y
pip install -U --no-cache-dir pip wheel setuptools virtualenv
pip install --no-cache-dir jupyterlab matplotlib nvidia-cudnn-cu11==8.6.* pandas pyts  scikit-learn tensorflow==2.12.* tensorrt
pip install --extra-index-url https://developer.download.nvidia.com/compute/redist --upgrade nvidia-dali-tf-plugin-cuda120

# Create conda environment variables
mkdir -p $CONDA_PREFIX/etc/conda/activate.d
echo 'CUDNN_PATH=$(dirname $(python -c "import nvidia.cudnn;print(nvidia.cudnn.__file__)"))' >> $CONDA_PREFIX/etc/conda/activate.d/env_vars.sh
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/:$CUDNN_PATH/lib' >> $CONDA_PREFIX/etc/conda/activate.d/env_vars.sh
source $CONDA_PREFIX/etc/conda/activate.d/env_vars.sh

# Verify install:
python3 -c "import tensorflow as tf; print(tf.config.list_physical_devices('GPU'))"

unset CONDA_ALWAYS_YES
