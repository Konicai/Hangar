FROM node:19-alpine

USER node
WORKDIR hangar-frontend
ENV TERM xterm-256color
ENV HOST 0.0.0.0

EXPOSE 1337
ENTRYPOINT ["./entrypoint.sh"]

COPY --chown=node:node --chmod=744 /chart/dockerfiles/frontend/entrypoint.sh /hangar-frontend/entrypoint.sh
COPY --chown=node:node /frontend/ /hangar-frontend/
