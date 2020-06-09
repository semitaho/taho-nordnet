exports.helloBackground = (data, context) => {
  return `Hello ${data.name || 'World'}!`;
};