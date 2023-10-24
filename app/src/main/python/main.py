# vulcan_login.py
import sys
import asyncio
from vulcan import Keystore, Account, Vulcan

async def login_vulcan(token, symbol, pin):
    keystore = await Keystore.create()
    with open("keystore.json", "w") as f:
        f.write(keystore.as_json)

    with open("keystore.json") as f:
        keystore = Keystore.load(f)

    account = await Account.register(keystore, token, symbol, pin)
    with open("account.json", "w") as f:
        f.write(account.as_json)

    with open("account.json") as f:
        account = Account.load(f)

    client = Vulcan(keystore, account)

    await client.select_student()

    lucky_number = await client.data.get_lucky_number()
    print("Lucky Number:", lucky_number)

    await client.close()

if __name__ == "__main__":
    asyncio.run(login_vulcan("3S13JA9", "https://uonetplus.vulcan.net.pl/powiatgostynski", "963158"))
