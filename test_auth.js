const https = require('http');

const data = JSON.stringify({
  username: "admin",
  password: "password123"
});

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
};

const req = https.request(options, res => {
  console.log(`statusCode: ${res.statusCode}`);
  let responseData = '';

  res.on('data', d => {
    responseData += d;
  });

  res.on('end', () => {
    console.log(`Response: ${responseData}`);
  });
});

req.on('error', error => {
  console.error(error);
});

req.write(data);
req.end();
