#!/usr/bin/env bash

set -e

test "Z${USERUID}" = "Z" && USERUID="${USER_UID:-"automatic"}"
test "Z${USERGID}" = "Z" && USERGID="${USER_GID:-"automatic"}"

test "Z"

if [ "Z${USERNAME}" = "Z" ]; then
    USERNAME="root"
    USERUID="0"
    USERGID="0"
fi

group_name="${USERNAME}"
if id -u ${USERNAME} > /dev/null 2>&1; then
    # User exists, update if needed
    if [ "${USERGID}" != "automatic" ] && [ "$USERGID" != "$(id -g $USERNAME)" ]; then
        group_name="$(id -gn $USERNAME)"
        groupmod --gid $USERGID ${group_name}
        usermod --gid $USERGID $USERNAME
    fi
    if [ "${USERUID}" != "automatic" ] && [ "$USERUID" != "$(id -u $USERNAME)" ]; then
        usermod --uid $USERUID $USERNAME
    fi
else
    # Create user
    if [ "${USERGID}" = "automatic" ]; then
        groupadd ${group_name}
    else
        groupadd --gid $USERGID ${group_name}
    fi
    if [ "${USERUID}" = "automatic" ]; then
        useradd -s /bin/bash --gid ${group_name} -m $USERNAME
    else
        useradd -s /bin/bash --uid $USERUID --gid ${group_name} -m $USERNAME
    fi
fi