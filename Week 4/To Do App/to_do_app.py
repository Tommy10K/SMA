import tkinter as tk
from tkinter import messagebox
import sqlite3

conn = sqlite3.connect("todos.db")
cur = conn.cursor()
cur.execute("""
CREATE TABLE IF NOT EXISTS todos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT
)
""")
conn.commit()

def renumber_todos():
    cur.execute("SELECT id FROM todos ORDER BY id ASC")
    ids = [row[0] for row in cur.fetchall()]
    for new_id, old_id in enumerate(ids, start=1):
        cur.execute("UPDATE todos SET id = ? WHERE id = ?", (new_id, old_id))
    conn.commit()

def load_todos():
    listbox.delete(0, tk.END)
    cur.execute("SELECT id, title FROM todos ORDER BY id ASC")
    for todo_id, title in cur.fetchall():
        listbox.insert(tk.END, f"{todo_id}. {title}")

def open_todo_dialog(title_text="Add To-Do", init_title="", init_desc=""):
    dialog = tk.Toplevel(root)
    dialog.title(title_text)
    dialog.geometry("400x300")
    dialog.grab_set()

    tk.Label(dialog, text="Title:").pack(anchor="w", padx=10, pady=(10, 0))
    title_entry = tk.Text(dialog, height=2, width=45, wrap="word")
    title_entry.insert("1.0", init_title)
    title_entry.pack(padx=10, pady=5)

    tk.Label(dialog, text="Description:").pack(anchor="w", padx=10, pady=(10, 0))
    desc_entry = tk.Text(dialog, height=8, width=45, wrap="word")
    desc_entry.insert("1.0", init_desc)
    desc_entry.pack(padx=10, pady=5)

    result = {"title": None, "desc": None}

    def save_and_close():
        result["title"] = title_entry.get("1.0", "end").strip()
        result["desc"] = desc_entry.get("1.0", "end").strip()
        dialog.destroy()

    def cancel():
        dialog.destroy()

    btn_frame = tk.Frame(dialog)
    btn_frame.pack(pady=10)
    tk.Button(btn_frame, text="Save", command=save_and_close, width=10).grid(row=0, column=0, padx=10)
    tk.Button(btn_frame, text="Cancel", command=cancel, width=10).grid(row=0, column=1, padx=10)

    dialog.wait_window()  # wait for close
    return result["title"], result["desc"]

def add_todo():
    title, desc = open_todo_dialog("Add To-Do")
    if not title:
        return
    cur.execute("INSERT INTO todos (title, description) VALUES (?, ?)", (title, desc))
    conn.commit()
    renumber_todos()
    load_todos()

def edit_todo():
    selection = listbox.curselection()
    if not selection:
        messagebox.showwarning("Edit", "Select a task first.")
        return
    index = selection[0]
    item_text = listbox.get(index)
    todo_id = int(item_text.split(".")[0])

    cur.execute("SELECT title, description FROM todos WHERE id = ?", (todo_id,))
    row = cur.fetchone()
    if not row:
        return

    old_title, old_desc = row
    new_title, new_desc = open_todo_dialog("Edit To-Do", old_title, old_desc)
    if not new_title:
        return

    cur.execute("UPDATE todos SET title = ?, description = ? WHERE id = ?", (new_title, new_desc, todo_id))
    conn.commit()
    load_todos()

def delete_todo():
    selection = listbox.curselection()
    if not selection:
        messagebox.showwarning("Delete", "Select a task first.")
        return
    index = selection[0]
    item_text = listbox.get(index)
    todo_id = int(item_text.split(".")[0])

    if messagebox.askyesno("Delete", "Are you sure you want to delete this task?"):
        cur.execute("DELETE FROM todos WHERE id = ?", (todo_id,))
        conn.commit()
        renumber_todos()
        load_todos()

def view_todo():
    selection = listbox.curselection()
    if not selection:
        messagebox.showinfo("View", "Select a task first.")
        return
    index = selection[0]
    item_text = listbox.get(index)
    todo_id = int(item_text.split(".")[0])
    cur.execute("SELECT title, description FROM todos WHERE id = ?", (todo_id,))
    row = cur.fetchone()
    if row:
        title, desc = row
        messagebox.showinfo(title, desc or "(No description)")

root = tk.Tk()
root.title("To-Do List")
root.geometry("400x500")

frame = tk.Frame(root)
frame.pack(padx=10, pady=10, fill=tk.BOTH, expand=True)

scrollbar = tk.Scrollbar(frame)
scrollbar.pack(side=tk.RIGHT, fill=tk.Y)

listbox = tk.Listbox(frame, yscrollcommand=scrollbar.set, font=("Arial", 12))
listbox.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
scrollbar.config(command=listbox.yview)

listbox.bind("<Double-1>", lambda e: view_todo())

btn_frame = tk.Frame(root)
btn_frame.pack(pady=5)

tk.Button(btn_frame, text="Add", width=10, command=add_todo).grid(row=0, column=0, padx=5)
tk.Button(btn_frame, text="Edit", width=10, command=edit_todo).grid(row=0, column=1, padx=5)
tk.Button(btn_frame, text="Delete", width=10, command=delete_todo).grid(row=0, column=2, padx=5)
tk.Button(btn_frame, text="View", width=10, command=view_todo).grid(row=0, column=3, padx=5)

load_todos()
root.mainloop()
conn.close()