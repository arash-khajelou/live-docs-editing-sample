document.addEventListener('DOMContentLoaded', fetchDocuments);

let currentDocId = null;
let ws = null;
let currentDocumentContent = "";
let cursorIndex = 0;

function fetchDocuments() {
    // Assuming a REST endpoint that returns a list of documents
    fetch('/api/documents')
        .then(response => response.json())
        .then(data => {
            const docList = document.getElementById('docList');
            docList.innerHTML = ''; // Clear existing list
            data.forEach(doc => {
                const docItem = document.createElement('div');
                docItem.className = 'doc-item';
                docItem.innerHTML = `${doc.title} 
                                     <button onclick="editDocument(${doc.id})">Edit</button>
                                     <button onclick="viewDocument(${doc.id})">View</button>`;
                docList.appendChild(docItem);
            });
        });
}

function showCreateDocument() {
    document.getElementById('createDocument').style.display = 'block';
}

function createDocument() {
    const docName = document.getElementById('newDocName').value;
    fetch('/api/documents', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({title: docName})
    }).then(() => {
        fetchDocuments();
        document.getElementById('createDocument').style.display = 'none';
        document.getElementById('newDocName').value = ''; // Reset input
    });
}

function editDocument(docId) {
    currentDocId = docId;

    // Assuming a REST endpoint that returns a list of documents
    fetch(`/api/edit-session/init/${docId}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('viewer').style.display = 'none';
            document.getElementById('editor-container').style.display = 'block';
            const editor = document.getElementById('editor');
            editor.innerHTML = data.content; // Reset editor content

            // Setup WebSocket for editing
            setupWebSocket(docId, true);
        });
}

function viewDocument(docId) {
    currentDocId = docId;

    fetch(`/api/edit-session/view/${docId}`)
        .then(response => response.text())
        .then(content => {
            console.log(content);
            document.getElementById('editor-container').style.display = 'none';
            const viewer = document.getElementById('viewer');
            viewer.style.display = 'block';
            viewer.innerHTML = content;

            // Setup WebSocket for viewing
            setupWebSocket(docId, false);
        });

}

function setupWebSocket(docId, isEditor) {
    if (ws) {
        ws.close(); // Close any existing connection
    }

    ws = new WebSocket(`ws://localhost:8080/ws-doc-edit/${docId}`);

    ws.onopen = function () {
        document.getElementById("status").textContent = "Connected";
    };

    ws.onclose = function () {
        document.getElementById("status").textContent = "Disconnected";
    };

    ws.onerror = function (err) {
        console.error("WebSocket error observed:", err);
    };

    ws.onmessage = function (event) {
        if (!isEditor) {
            const data = JSON.parse(event.data);
            applyEvent(data);
        }
    };

    if (isEditor) {
        const editor = document.getElementById('editor');
        editor.addEventListener('input', function (e) {
            const cursorPosition = e.target.selectionStart;
            const previousContent = currentDocumentContent;
            const currentContent = e.target.value;
            currentDocumentContent = currentContent; // Update the current document content

            if (currentContent.length < previousContent.length) {
                // DELETE event
                const deletedLength = previousContent.length - currentContent.length;
                const fromIndex = cursorPosition;
                const toIndex = fromIndex + deletedLength;
                sendEvent(docId, 'DELETE', fromIndex, toIndex, '');
            } else if (currentContent.length > previousContent.length) {
                // INSERT event
                const insertedChar = currentContent[cursorPosition - 1];
                sendEvent(docId, 'INSERT', cursorPosition - 1, cursorPosition, insertedChar);
            }
        });

        editor.addEventListener('keydown', function (e) {
            if (e.key === 'Backspace' || e.key === 'Delete') {
                const fromIndex = e.target.selectionStart;
                const toIndex = e.target.selectionEnd;
                sendEvent(docId, 'DELETE', fromIndex, toIndex, ''); // Handle deletion with Backspace/Delete keys
            }
        });
    }
}

function sendEvent(docId, type, fromIndex, toIndex, metadata) {
    const event = {docId, type, fromIndex, toIndex, metadata};
    ws.send(JSON.stringify(event));
}

function applyEvent(event) {
    console.log(event);
    // This function should apply incoming events to the content
    const target = document.getElementById('viewer');
    if (event.type === 'INSERT') { // INSERT
        target.innerHTML = target.innerHTML.slice(0, event.fromIndex) + event.metadata + target.innerHTML.slice(event.toIndex);
    } else if (event.type === 'DELETE') { // DELETE
        target.innerHTML = target.innerHTML.slice(0, event.fromIndex) + target.innerHTML.slice(event.toIndex);
    }
}

function saveCurrent() {
    fetch(`/api/edit-session/save/${currentDocId}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
    }).then(() => {
        console.log('Document saved');
    });
}

function endEditSession() {
    fetch(`/api/edit-session/end/${currentDocId}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
    }).then(() => {
        console.log('Edit session ended');
        document.getElementById("editor-container").style.display = "none";
    });
}
