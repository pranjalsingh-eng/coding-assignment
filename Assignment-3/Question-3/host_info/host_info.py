"""
host_info.py
------------
Abstract base class for host hardware information.
"""

import json
from abc import ABC, abstractmethod


class HostInfo(ABC):
    def __init__(self):
        self._info: dict = {}

    @abstractmethod
    def get_hardware_info(self) -> None:
        """Populate self._info with hardware data."""

    def display_hardware_info(self) -> None:
        """Print collected hardware info as formatted JSON."""
        print(json.dumps(self._info, indent=4))
