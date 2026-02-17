#!/usr/bin/env python3

import os
import pathlib
import sys

import pandas as pd
from asciimatics.exceptions import NextScene, ResizeScreenError, StopApplication
from asciimatics.scene import Scene
from asciimatics.screen import Screen
from asciimatics.widgets import Button, Divider, Frame, Layout, ListBox, Text, Widget
from view_results import close_plots, plt_data, plt_show

# https://github.com/peterbrittain/asciimatics/blob/master/samples/contact_list.py


class EntryModel(object):
    def __init__(self):
        self.files_path = pathlib.Path("measurements_directory")
        self.current_id = None

        # read anomalies
        self.init()

    def init(self):
        self.init_dirs()
        self.dir_size = len(self.dirs)
        self.directories_indexes = list(zip(self.dirs, range(0, self.dir_size)))
        self.jump_size = self.dir_size // 10
        # print(self.directories_indexes)

    def init_dirs(self):
        self.dirs = os.listdir(self.files_path)
        self.dirs.sort()

    # (name key)
    def get_dirs(self):
        return self.directories_indexes

    def open_dir(self, num=1):
        if self.current_id is None:
            return

        choice = self.directories_indexes[self.current_id][0]
        if choice.endswith(".csv"):
            self.open_graph(num)
            return

        self.files_path = self.files_path.joinpath(choice)
        self.init()

    def open_graph(self, num=1):
        if self.current_id is None:
            return

        graph_count = 1
        for i in range(self.current_id, self.current_id + num):
            if i >= len(self.directories_indexes):
                return
            key, _ = self.directories_indexes[i]
            key_name = f"{self.files_path.name}_{key}"
            tmp_path = self.files_path.joinpath(key)
            try:
                data = pd.read_csv(tmp_path)
            except Exception:
                data = pd.read_csv(tmp_path, sep="\t")
            plt_data(data["x"], data["y"], data["z"], data["t"], key_name, graph_count)
            graph_count += 1
        plt_show()

    def back_pressed(self):
        self.files_path = self.files_path.parent
        self.init()


class ListView(Frame):
    last = None
    last_stack = []

    def __init__(self, screen, model):
        super(ListView, self).__init__(
            screen,
            screen.height * 5 // 6,
            screen.width * 5 // 6,
            on_load=self._reload_list,
            hover_focus=True,
            can_scroll=False,
            title="Entry List",
        )
        # Save off the model that accesses the entries database.
        self._model = model

        # Create the form for displaying the list of entries.
        self._list_view = ListBox(
            Widget.FILL_FRAME,
            model.get_dirs(),
            name="entries",
            add_scroll_bar=True,
            on_change=self._on_pick,
            on_select=self._view,
        )
        self._view_button = Button("Open", self._view)
        self._back_button = Button("Back", self._back)
        self._jump_button = Button("Jump", self._jump)
        self._view_5_button = Button("View 5", self._view_5)
        self._view_all_button = Button("View All", self._view_all)
        layout = Layout([100], fill_frame=True)
        self.add_layout(layout)
        layout.add_widget(self._list_view)
        layout.add_widget(Divider())
        layout2 = Layout([2, 1, 1, 1, 1, 1])
        self.add_layout(layout2)
        layout2.add_widget(self._view_button, 0)
        layout2.add_widget(self._back_button, 1)
        layout2.add_widget(self._jump_button, 2)
        layout2.add_widget(self._view_5_button, 3)
        layout2.add_widget(self._view_all_button, 4)
        layout2.add_widget(Button("Quit", self._quit), 5)
        self.fix()
        self._on_pick()

    def _on_pick(self):
        is_empty = self._list_view.value is None
        self._view_button.disabled = is_empty
        self._back_button.disabled = is_empty
        self._jump_button.disabled = is_empty
        self._view_all_button.disabled = is_empty

    def _reload_list(self, new_value=None):
        self._list_view.options = self._model.get_dirs()
        if new_value:
            self._list_view.value = new_value
        else:
            self._list_view.value = self.last

    def _view(self):
        tmp_last = self._list_view.value
        self.save()
        choice_name = self._list_view.options[self.data["entries"]][0]
        if os.path.isdir(self._model.files_path.joinpath(choice_name)):  # save last just for dir browsing not files
            self.last = 0
            self.last_stack.append(tmp_last)
        else:
            self.last = tmp_last
        self._model.current_id = self.data["entries"]
        self._model.open_dir()
        self._reload_list()

    def _back(self):
        if self.last_stack:
            self.last = self.last_stack.pop()
        else:
            self.last = None
        self._model.back_pressed()
        self._reload_list()

    def _view_all(self):
        self._model.current_id = 0
        self._model.open_dir(len(self._model.get_dirs()))

    def _view_5(self):
        self.save()
        self._model.current_id = self.data["entries"]
        self._model.open_dir(5)

    def _jump(self):
        self._reload_list((self._model.jump_size + self._list_view.value) % self._model.dir_size)

    def _delete(self):
        self._model.delete_entry(self.data["entries"])
        self._reload_list()

    @staticmethod
    def _quit():
        close_plots()
        raise StopApplication("User pressed quit")


class EntryView(Frame):
    def __init__(self, screen, model):
        super(EntryView, self).__init__(
            screen,
            screen.height * 5 // 6,
            screen.width * 5 // 6,
            hover_focus=True,
            can_scroll=False,
            title="Entry Details",
            reduce_cpu=True,
        )
        # Save off the model that accesses the entries database.
        self._model = model

        # Create the form for displaying the list of entries.
        layout = Layout([100], fill_frame=True)
        self.add_layout(layout)
        layout.add_widget(Text("Phone:", "phone"))
        layout.add_widget(Text("Freq:", "freq"))
        layout.add_widget(Text("Sensor:", "sensor"))
        layout.add_widget(Text("Date:", "date"))
        layout2 = Layout([1, 1, 1, 1, 1])
        self.add_layout(layout2)
        layout2.add_widget(Button("Go back", self._back), 0)
        # self._model.open_dir()
        self.fix()

    def reset(self):
        # Do standard reset to clear out form, then populate with new data.
        super(EntryView, self).reset()
        self.data = self._model.get_current_entry()

    def _ok(self):
        self.save()
        # self._model.update_current_entry(self.data) push new info
        raise NextScene("Main")

    @staticmethod
    def _back():
        raise NextScene("Main")


def viewEntries(screen, scene):
    scenes = [
        Scene([ListView(screen, entries)], -1, name="Main"),
        Scene([EntryView(screen, entries)], -1, name="View Entry"),
    ]

    screen.play(scenes, stop_on_resize=True, start_scene=scene, allow_int=True)


entries = EntryModel()


def main():
    last_scene = None
    while True:
        try:
            Screen.wrapper(viewEntries, catch_interrupt=True, arguments=[last_scene])
            sys.exit(0)
        except ResizeScreenError as e:
            last_scene = e.scene


if __name__ == "__main__":
    main()
