FROM python

WORKDIR /app

# RUN apt update && apt install -y zsh wget curl
# RUN wget https://github.com/robbyrussell/oh-my-zsh/raw/master/tools/install.sh -O - | zsh || true

COPY requirements.txt ./

RUN pip3 install --no-cache-dir -r requirements.txt

COPY . ./

# CMD ["zsh"]
