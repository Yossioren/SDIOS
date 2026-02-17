from Crypto.PublicKey import RSA

private_key = RSA.generate(4096)
with open("model_controller/private_key.pem", "wb") as f:
    f.write(private_key.export_key("PEM"))

public_key = private_key.publickey()
with open("server/public_key.pem", "wb") as f:
    f.write(public_key.exportKey("PEM"))
