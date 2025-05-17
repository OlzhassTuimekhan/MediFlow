import gradio as gr
from huggingface_hub import InferenceClient
import os

API_TOKEN = os.environ.get("API_TOKEN")

client = InferenceClient("HuggingFaceH4/zephyr-7b-beta", token=API_TOKEN)


def respond(message, system_message, max_tokens, temperature, top_p, language_choice):
    if language_choice != "English":
        system_message += f"\nPlease answer in {language_choice}."

    messages = [{"role": "system", "content": system_message}]
    messages.append({"role": "user", "content": message})

    result = client.chat_completion(
        messages,
        max_tokens=max_tokens,
        temperature=temperature,
        top_p=top_p,
        stream=False
    )

    response_text = result.choices[0].message["content"]
    return response_text


iface = gr.Interface(
    fn=respond,
    inputs=[
        gr.Textbox(label="Введите ваш вопрос"),
        gr.Textbox(
            value=(
                "You are a helpful assistant for the MediFlow mobile app. "
                "The app has the following sections:\n"
                "- My Documents: you can see all your medical documents.\n"
                "- Appointments: schedule or view doctor appointments.\n"
                "- AI Assistant: chat for health/app questions.\n"
                "- Doctors: list of doctors, specialties, and booking option.\n"
                "- Profile: user settings, personal info, change information about user.\n"
                "\n"
                "When the user describes symptoms, recommend a suitable doctor. "
                "Then remind them about booking in the 'Appointments' tab or searching in the 'Doctors' tab.\n"
                "If the user mentions documents, refer them to 'My Documents'. "
                "For scheduling, refer them to 'Appointments' or 'Doctors'. "
                "For profile or account changes, mention 'Profile'.\n"
                "If the user requests a specific language, answer in that language. "
                "Otherwise, default to English.\n"
                "Keep your answers concise, friendly, and helpful."
            ),
            label="System Message (App Instructions)"
        ),
        gr.Slider(minimum=1, maximum=2048, value=512, step=1, label="Max new tokens"),
        gr.Slider(minimum=0.1, maximum=4.0, value=0.7, step=0.1, label="Temperature"),
        gr.Slider(minimum=0.1, maximum=1.0, value=0.95, step=0.05, label="Top-p (nucleus sampling)"),
        gr.Dropdown(
            choices=["English", "Russian", "Spanish", "German"],
            value="English",
            label="Language Choice"
        )
    ],
    outputs="text",
    title="MediFlow AI Assistant",
    description="Задавайте вопросы по здоровью и по функционалу MediFlow."
)

if __name__ == "__main__":
    iface.launch()