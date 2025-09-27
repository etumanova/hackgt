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
    localStorage.setItem('chatbotOpen', !currentlyOpen);
    
    // Focus on input when opening
    if (!currentlyOpen) {
      inputField.focus();
    }
  });

  closeBtn.addEventListener("click", () => {
    windowBox.style.display = "none";
    localStorage.setItem('chatbotOpen', false);
  });

  function addMessage(sender, text, isError = false) {
    const msgDiv = document.createElement("div");
    msgDiv.style.margin = "5px 0";
    msgDiv.style.padding = "8px";
    msgDiv.style.borderRadius = "8px";
    
    if (sender === "You") {
      msgDiv.style.backgroundColor = "#7e58d6";
      msgDiv.style.color = "white";
      msgDiv.style.textAlign = "right";
    } else {
      msgDiv.style.backgroundColor = isError ? "#ffebee" : "#f1f1f1";
      msgDiv.style.color = isError ? "#d32f2f" : "#333";
    }
    
    msgDiv.innerHTML = `<strong>${sender}:</strong> ${text}`;
    messages.appendChild(msgDiv);
    
    // Save to localStorage
    localStorage.setItem('chatbotMessages', messages.innerHTML);
    messages.scrollTop = messages.scrollHeight;
  }

  async function sendMessage() {
    const text = inputField.value.trim();
    if (!text) return;

    // Disable input while processing
    inputField.disabled = true;
    sendBtn.disabled = true;
    sendBtn.textContent = "Sending...";

    // Display user message
    addMessage("You", text);
    inputField.value = "";

    try {
      console.log("Sending message to API:", text); // Debug log
      
      const response = await fetch("/api/gemini/chat", {
        method: "POST",
        headers: { 
          "Content-Type": "application/json",
          "Accept": "application/json"
        },
        body: JSON.stringify({ message: text })
      });

      console.log("Response status:", response.status); // Debug log

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      console.log("Response data:", data); // Debug log

      if (data.reply) {
        addMessage("Gemini", data.reply);
      } else {
        addMessage("Gemini", "I received your message but couldn't generate a response.", true);
      }

    } catch (error) {
      console.error("Chat error:", error);
      
      let errorMessage = "Sorry, I'm having trouble connecting. ";
      if (error.message.includes("404")) {
        errorMessage += "The chat service isn't available right now.";
      } else if (error.message.includes("500")) {
        errorMessage += "There's a server issue. Please try again in a moment.";
      } else {
        errorMessage += "Please check your internet connection and try again.";
      }
      
      addMessage("Gemini", errorMessage, true);
    } finally {
      // Re-enable input
      inputField.disabled = false;
      sendBtn.disabled = false;
      sendBtn.textContent = "Send";
      inputField.focus();
    }
  }

  sendBtn.addEventListener("click", sendMessage);
  inputField.addEventListener("keypress", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  // Add welcome message on first load
  if (!savedMessages) {
    addMessage("Gemini", "Hi! I'm your AI financial assistant. Ask me anything about budgeting, saving, or managing money as a student! 💰");
  }
});