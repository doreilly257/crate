# -*- coding: utf-8 -*-

import socket
from threading import Lock


def public_ipv4():
    """
    take first public interface
    sorted by getaddrinfo - see RFC 3484
    should have the real public IPv4 address as first address.
    At the moment the test layer is not able to handle v6 addresses
    """
    for addrinfo in socket.getaddrinfo(socket.gethostname(), None):
        if addrinfo[1] in (socket.SOCK_STREAM, socket.SOCK_DGRAM) and addrinfo[0] == socket.AF_INET:
            return addrinfo[4][0]


class PortPool(object):
    """
    Pool that returns a unique available port
    reported by the kernel.
    """

    MAX_RETRIES = 10

    def __init__(self):
        self.ports = set()
        self.lock = Lock()

    def random_available_port(self):
        sock = socket.socket()
        sock.bind(('', 0))
        port = sock.getsockname()[1]
        sock.close()
        return port

    def get(self):
        retries = 0
        port = self.random_available_port()

        with self.lock:
            while port in self.ports:
                port = self.random_available_port()
                retries += 1
                if retries > self.MAX_RETRIES:
                    raise OSError("Could not get free port. Max retries exceeded.")
            self.ports.add(port)
        return port


GLOBAL_PORT_POOL = PortPool()

