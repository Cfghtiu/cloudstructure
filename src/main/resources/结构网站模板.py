from flask import Flask, request, jsonify, send_from_directory

app = Flask("demo")


@app.route("/get_token", methods=["POST"])
def token():
    """客户端向网站发送请求获得授权码，再将授权码发送给MC服务器"""
    data = request.get_json()
    user: str = data["user"]
    passwd: str = data["passwd"]

    method: str = data["method"]  # 如果客户端是想上传文件，则值为"update"，下载则为"download"
    token = user+"123"  # 授权码应该是随机生成的，这里只是举例
    return jsonify(code=200, token=token)


@app.route("/upload", methods=["POST"])
def upload():
    """MC服务器利用客户端申请到的授权码以用户的名义上传结构数据"""
    token = request.args["token"]
    path = request.args["path"]
    file = request.files["file"]
    # 获得用户
    user = token[:-3]

    file.save(user+"/"+path+".nbt")
    return jsonify(code=-400, message="上传失败")


@app.route("/download", methods=["GET"])
def download():
    """MC服务器下载结构"""
    token = request.args["token"]
    path = request.args["path"]

    user = token[:-3]
    return send_from_directory("", user+"/"+path+".nbt")


if __name__ == '__main__':
    app.run()
