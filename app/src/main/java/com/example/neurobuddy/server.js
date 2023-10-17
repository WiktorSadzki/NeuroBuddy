const express = require('express');
const cors = require('cors');
const axios = require('axios');

const app = express();

app.use(cors());

app.get('/login', async (req, res) => {
  try {
    const response = await axios.post('https://cufs.vulcan.net.pl/powiatgostynski/Account/LogOn?ReturnUrl=%2Fpowiatgostynski%2FFS%2FLS%3Fwa%3Dwsignin1.0%26wtrealm%3Dhttps%253A%252F%252Feduone.pl%252Fpowiatgostynski%252FLoginEndpoint.aspx%26wctx%3Dhttps%253A%252F%252Feduone.pl%252Fpowiatgostynski%252FLoginEndpoint.aspx', req.query);
    res.send(response.data);
  } catch (error) {
    res.status(500).send(error.message);
  }
});

const port = 3000;
app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
