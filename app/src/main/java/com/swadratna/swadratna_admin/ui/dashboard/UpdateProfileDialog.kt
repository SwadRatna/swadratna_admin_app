package com.swadratna.swadratna_admin.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.swadratna.swadratna_admin.data.model.RestaurantProfileRequest
import com.swadratna.swadratna_admin.data.model.OtherDetails

@Composable
fun UpdateProfileDialog(
    onDismiss: () -> Unit,
    onSubmit: (RestaurantProfileRequest) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }
    var hqMobileNo by remember { mutableStateOf("") }
    var fassaiLicenceNo by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var youtubeDescription by remember { mutableStateOf("") }
    var hqEmail by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Update Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = gstNumber,
                    onValueChange = { gstNumber = it },
                    label = { Text("GST Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = hqMobileNo,
                    onValueChange = { hqMobileNo = it },
                    label = { Text("HQ Mobile No") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = fassaiLicenceNo,
                    onValueChange = { fassaiLicenceNo = it },
                    label = { Text("FASSAI Licence No") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("YouTube URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = youtubeDescription,
                    onValueChange = { youtubeDescription = it },
                    label = { Text("YouTube Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = hqEmail,
                    onValueChange = { hqEmail = it },
                    label = { Text("HQ Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = { Text("Instagram") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmit(
                                RestaurantProfileRequest(
                                    name = name,
                                    gstNumber = gstNumber,
                                    hqMobileNo = hqMobileNo,
                                    fassaiLicenceNo = fassaiLicenceNo,
                                    youtubeUrl = youtubeUrl,
                                    youtubeDescription = youtubeDescription,
                                    hqEmail = hqEmail,
                                    otherDetails = OtherDetails(instagram = instagram)
                                )
                            )
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}
