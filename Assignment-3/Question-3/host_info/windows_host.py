"""
windows_host.py
---------------
WindowsHost: collects hardware info on Windows via psutil + platform.
"""

import platform

try:
    import psutil
    _PSUTIL = True
except ImportError:
    _PSUTIL = False

from .host_info import HostInfo


class WindowsHost(HostInfo):
    def get_hardware_info(self) -> None:
        self._info = {
            "cpu": self._cpu_info(),
            "memory": self._memory_info(),
            "disk": self._disk_info(),
            "os": self._os_info(),
        }

    # ------------------------------------------------------------------
    def _cpu_info(self) -> dict:
        info = {"architecture": platform.machine()}
        if _PSUTIL:
            info["physical_cores"] = psutil.cpu_count(logical=False)
            info["logical_processors"] = psutil.cpu_count(logical=True)
            info["current_freq_mhz"] = (
                psutil.cpu_freq().current if psutil.cpu_freq() else None
            )
        return info

    def _memory_info(self) -> dict:
        if not _PSUTIL:
            return {}
        vm = psutil.virtual_memory()
        return {
            "total_bytes": vm.total,
            "available_bytes": vm.available,
            "used_bytes": vm.used,
            "percent": vm.percent,
        }

    def _disk_info(self) -> list:
        if not _PSUTIL:
            return []
        disks = []
        for part in psutil.disk_partitions(all=False):
            try:
                usage = psutil.disk_usage(part.mountpoint)
                disks.append({
                    "device": part.device,
                    "mountpoint": part.mountpoint,
                    "fstype": part.fstype,
                    "total_bytes": usage.total,
                    "used_bytes": usage.used,
                    "free_bytes": usage.free,
                    "percent": usage.percent,
                })
            except PermissionError:
                pass
        return disks

    def _os_info(self) -> dict:
        return {
            "system": platform.system(),
            "release": platform.release(),
            "version": platform.version(),
            "node": platform.node(),
        }
