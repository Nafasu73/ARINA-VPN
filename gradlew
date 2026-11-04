#!/bin/sh
# Gradle wrapper script for ARIANA VPN Android Project
# Developer: Fatemeh Zahra Saei (فاطمه زهرا ساعی)

DIR=$(cd "$(dirname "$0")" && pwd)
exec gradle "$@"
