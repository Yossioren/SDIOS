import getopt
import json
import shutil
import sys
import time
from datetime import datetime
from pathlib import Path

# patcher wrote as
# path
# dest
# if start with ? it will copy the whole dir
# # are comments
BACKUP_DIR = Path("./backups")


def usage():
    print("usage app.py -c conf -d <dir>")
    exit(-1)


class ConfigurationPatcher:
    def __init__(self, conf_path: Path, dir_path: Path):
        self.dir_path = dir_path
        self.conf_path = conf_path
        self.conf = []
        self._setup_patcher_configuration()
        self.backup_dest = BACKUP_DIR / self.conf_path / str(time.time_ns())
        self.backup_dest.mkdir(parents=True, exist_ok=True)

    def _setup_patcher_configuration(self):
        text = self.conf_path.read_text().strip().split("\n")
        content = []
        for x in text:
            x = x.strip()
            if x and not x.startswith("#"):
                x = x.replace("\\", "/")
                if x.endswith("/"):
                    x = x[:-1]
                content.append(x)
        assert len(content) % 3 == 0, "bad .conf, should have action-source-dest"

        for i in range(0, len(content), 3):
            self.conf.append(
                {
                    "source": content[i],
                    "dest": f"{self.dir_path}/{content[i+1]}",
                    "action": content[i + 2],
                }
            )
        print(json.dumps(self.conf, indent=4))

    def replace_files(self):
        for i, conf in enumerate(self.conf):
            action = conf["action"]
            source = Path(conf["source"])
            dest = Path(conf["dest"])
            dest.parent.mkdir(parents=True, exist_ok=True)
            if action == ">":  # file not exist
                dest.unlink(missing_ok=True)
                dest.write_bytes(source.read_bytes())
            elif action == "?":  # copy dir (dir not exist)
                shutil.copytree(source, dest, dirs_exist_ok=True)
            elif action == "_":  # replace file
                dest.replace(self.backup_dest / str(dest).replace("/", "_"))
                dest.write_bytes(source.read_bytes())
            else:
                assert False, f"No such action {action}, rule {i}"


def main():
    try:
        opts, _ = getopt.getopt(sys.argv[1:], "hd: hc:")
    except getopt.GetoptError as e:
        print(e)
        usage()

    # print(opts)
    dt_string = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
    print(f"start {dt_string}")
    conf_path = None
    dir_path = None
    for opt, arg in opts:
        if opt == "-d":
            dir_path = Path(arg).expanduser()
        elif opt == "-c":
            conf_path = Path(arg).expanduser()
    assert conf_path and dir_path, "Bad usage"
    if dir_path and dir_path.exists():
        patcher = ConfigurationPatcher(conf_path, dir_path)
        patcher.replace_files()
        print(
            "Whenever you change the api for the first time remember to update /frameworks/base/api/current.txt and run 'make update-api'\n"
            "For example to make SensorEvent constructor public you need also to add `ctor public SensorEvent(int);` in current.txt"
        )
    else:
        usage()


if __name__ == "__main__":
    main()
