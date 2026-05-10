"""
linux_host.py
-------------
LinuxHost: collects hardware info on Linux via /proc and /sys.
"""

import re
import subprocess

from .host_info import HostInfo


class LinuxHost(HostInfo):
    def get_hardware_info(self) -> None:
        self._info = {
            "cpu": self._cpu_info(),
            "memory": self._memory_info(),
            "disk": self._disk_info(),
            "os": self._os_info(),
        }

    # ------------------------------------------------------------------
    def _cpu_info(self) -> dict:
        info = {}
        try:
            with open("/proc/cpuinfo") as f:
                for line in f:
                    if line.startswith("model name") and "model_name" not in info:
                        info["model_name"] = line.split(":", 1)[1].strip()
                    if line.startswith("cpu cores") and "cores" not in info:
                        info["cores"] = int(line.split(":", 1)[1].strip())
        except OSError:
            pass
        try:
            with open("/proc/cpuinfo") as f:
                info["logical_processors"] = sum(
                    1 for l in f if l.startswith("processor")
                )
        except OSError:
            pass
        return info

    def _memory_info(self) -> dict:
        info = {}
        try:
            with open("/proc/meminfo") as f:
                for line in f:
                    key, _, val = line.partition(":")
                    val = val.strip()
                    if key == "MemTotal":
                        info["total_kb"] = int(val.split()[0])
                    elif key == "MemAvailable":
                        info["available_kb"] = int(val.split()[0])
        except OSError:
            pass
        return info

    def _disk_info(self) -> list:
        disks = []
        try:
            out = subprocess.check_output(
                ["df", "-h", "--output=source,size,used,avail,pcent,target"],
                text=True,
            )
            lines = out.strip().splitlines()
            headers = lines[0].split()
            for row in lines[1:]:
                parts = row.split()
                if len(parts) == len(headers):
                    disks.append(dict(zip(headers, parts)))
        except Exception:
            pass
        return disks

    def _os_info(self) -> dict:
        info = {}
        try:
            with open("/etc/os-release") as f:
                for line in f:
                    line = line.strip()
                    if "=" in line:
                        k, _, v = line.partition("=")
                        info[k] = v.strip('"')
        except OSError:
            pass
        return info
