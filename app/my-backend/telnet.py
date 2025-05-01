import telnetlib

host = '169.254.1.1'
username = 'cisco'
password = 'cisco'

tn = telnetlib.Telnet(host)

tn.read_until(b"Username:")
tn.write(username.encode('ascii') + b"\n")
tn.read_until(b"Password:")
tn.write(password.encode('ascii') + b"\n")

tn.write(b"enable\n")
tn.write(password.encode('ascii') + b"\n")

tn.write(b"show ip interface brief\n")

output = tn.read_all().decode('ascii')
print(output)

tn.write(b"exit\n")
