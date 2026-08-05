FROM silverpeas/silverdev:latest

ARG USER_GID=1000
ARG USER_UID=1000

# Create the silveruser user with the UID/GID above. If such a user (or such a group) already
# exists in the base image but with different identifiers, then change them instead of failing.
RUN set -eux; \
    if getent group silveruser > /dev/null; then \
      current_gid=$(getent group silveruser | cut -d: -f3); \
      if [ "${current_gid}" != "${USER_GID}" ]; then groupmod --gid ${USER_GID} silveruser; fi; \
    elif getent group ${USER_GID} > /dev/null; then \
      groupmod --new-name silveruser "$(getent group ${USER_GID} | cut -d: -f1)"; \
    else \
      groupadd --gid ${USER_GID} silveruser; \
    fi; \
    if id -u silveruser > /dev/null 2>&1; then \
      current_uid=$(id -u silveruser); \
      if [ "${current_uid}" != "${USER_UID}" ]; then usermod --uid ${USER_UID} silveruser; fi; \
      usermod --gid ${USER_GID} silveruser; \
      if [ -d /home/silveruser ]; then chown -R ${USER_UID}:${USER_GID} /home/silveruser; fi; \
    else \
      useradd --uid ${USER_UID} --gid ${USER_GID} -m silveruser; \
    fi;

USER silveruser

CMD ["/bin/bash"]

