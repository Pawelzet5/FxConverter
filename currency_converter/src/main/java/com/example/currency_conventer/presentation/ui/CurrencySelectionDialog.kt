package com.example.currency_conventer.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.presentation.state.CurrencyInputType
import com.example.currency_conventer.presentation.ui.theme.*
import com.example.currency_converter.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerBottomSheet(
    sheetState: SheetState,
    currencies: List<Currency>,
    currencyInputType: CurrencyInputType,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = dialogHandleColor) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(currencyInputType.labelTextResId),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = currencies,
                    key = { it.code }
                ) { currency ->
                    val horizontalPadding = 16.dp
                    CurrencyListItem(
                        currency = currency,
                        onClick = { onCurrencySelected(currency) },
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                    )
                    HorizontalDivider(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .height(1.dp)
                            .background(color = disabledColor)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyListItem(
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag
        Box(
            modifier = Modifier.size(48.dp)
                .background(color = disabledColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(CurrencyDefaults.getBigIconForCurrency(currency)),
                contentDescription = null,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(currency.countryNameResId),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    R.string.currency_display_format,
                    stringResource(currency.nameResId),
                    currency.code
                ),
                fontSize = 16.sp,
                color = secondaryTextColor
            )
        }
    }
}

