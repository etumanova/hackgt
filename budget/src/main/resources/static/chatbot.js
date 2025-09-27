document.addEventListener("DOMContentLoaded", function() {
  const button = document.getElementById("chatbot-button");
  const windowBox = document.getElementById("chatbot-window");
  const closeBtn = document.getElementById("chatbot-close");
  const inputField = document.getElementById("chatbot-text");
  const sendBtn = document.getElementById("chatbot-send");
  const messages = document.getElementById("chatbot-messages");

  button.addEventListener("click", () => {
    windowBox.style.display = (windowBox.style.display === "none" || windowBox.style.display === "") ? "flex" : "none";
  });

  closeBtn.addEventListener("click", () => {
    windowBox.style.display = "none";
  });

  async function sendMessage() {
    const text = inputField.value.trim();
    if (!text) return;

    // Display user message
    const userMsg = document.createElement("div");
    userMsg.textContent = "You: " + text;
    messages.appendChild(userMsg);

    inputField.value = "";
    messages.scrollTop = messages.scrollHeight;

    // Send to backend Gemini endpoint
    try {
      const response = await fetch("/api/gemini/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text })
      });
      const data = await response.json();

      const botMsg = document.createElement("div");
      botMsg.textContent = "Gemini: " + data.reply;
      messages.appendChild(botMsg);
      messages.scrollTop = messages.scrollHeight;
    } catch (error) {
      const botMsg = document.createElement("div");
      botMsg.textContent = "Gemini: Sorry, something went wrong.";
      messages.appendChild(botMsg);
    }
  }

  sendBtn.addEventListener("click", sendMessage);
  inputField.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendMessage();
  });
});
