document.addEventListener("DOMContentLoaded", function() {
  const button = document.getElementById("chatbot-button");
  const windowBox = document.getElementById("chatbot-window");
  const closeBtn = document.getElementById("chatbot-close");
  const inputField = document.getElementById("chatbot-text");
  const sendBtn = document.getElementById("chatbot-send");
  const messages = document.getElementById("chatbot-messages");

  // Load saved messages from localStorage
  const savedMessages = localStorage.getItem('chatbotMessages');
  if (savedMessages) {
    messages.innerHTML = savedMessages;
    messages.scrollTop = messages.scrollHeight;
  }

  // Load visibility state from localStorage
  const isOpen = localStorage.getItem('chatbotOpen') === 'true';
  windowBox.style.display = isOpen ? 'flex' : 'none';

  // Toggle chat visibility when button is clicked
  button.addEventListener("click", () => {
    const currentlyOpen = windowBox.style.display === "flex";
    windowBox.style.display = currentlyOpen ? "none" : "flex";
    localStorage.setItem('chatbotOpen', !currentlyOpen); // save state
  });

  closeBtn.addEventListener("click", () => {
    windowBox.style.display = "none";
    localStorage.setItem('chatbotOpen', false);
  });

  async function sendMessage() {
    const text = inputField.value.trim();
    if (!text) return;

    // Display user message
    const userMsg = document.createElement("div");
    userMsg.textContent = "You: " + text;
    messages.appendChild(userMsg);

    // Save updated messages to localStorage
    localStorage.setItem('chatbotMessages', messages.innerHTML);

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

      // Save bot reply to localStorage too
      localStorage.setItem('chatbotMessages', messages.innerHTML);
      messages.scrollTop = messages.scrollHeight;
    } catch (error) {
      const botMsg = document.createElement("div");
      botMsg.textContent = "Gemini: Sorry, something went wrong.";
      messages.appendChild(botMsg);
      localStorage.setItem('chatbotMessages', messages.innerHTML);
    }
  }

  sendBtn.addEventListener("click", sendMessage);
  inputField.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendMessage();
  });
});
