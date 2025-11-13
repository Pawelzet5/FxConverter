package com.example.currency_conventer.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.currency_conventer.domain.model.CurrencyDefaults
import com.example.currency_conventer.domain.model.dataclass.Currency
import com.example.currency_conventer.presentation.action.CurrencyExchangeScreenAction
import com.example.currency_conventer.presentation.state.*
import com.example.currency_conventer.presentation.ui.theme.*
import com.example.currency_conventer.presentation.viewmodel.CurrencyExchangeScreenViewModel
import com.example.currency_converter.R

@Composable
fun CurrencyExchangeScreenRoot(viewModel: CurrencyExchangeScreenViewModel = hiltViewModel()) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    CurrencyExchangeScreen(state, viewModel::onAction, Modifier.safeContentPadding())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyExchangeScreen(
    state: CurrencyExchangeScreenState,
    onAction: (CurrencyExchangeScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        CurrencyExchangeSection(
            state = state,
            onAction = onAction,
            modifier = Modifier
        )

        ErrorPanel(
            state = state.errorPanelState,
            onDismissClick = { onAction(CurrencyExchangeScreenAction.DismissErrorPanelClicked) },
            modifier = Modifier
        )
    }

    if (state.currencySelectionDialogState.isCurrencySelectionDialogOpen) {
        CurrencyPickerBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            currencies = state.availableCurrencies,
            currencyInputType = state.currencySelectionDialogState.currencyInputType,
            onCurrencySelected = {
                onAction(
                    CurrencyExchangeScreenAction.OnCurrencySelected(
                        it,
                        state.currencySelectionDialogState.currencyInputType
                    )
                )
            },
            onDismiss = { onAction(CurrencyExchangeScreenAction.SelectCurrencyDialogDismissed) },
        )
    }
}

@Composable
fun CurrencyExchangeSection(
    state: CurrencyExchangeScreenState,
    onAction: (CurrencyExchangeScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(16.dp)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(disabledColor, RoundedCornerShape(16.dp)),
            ) {
                SendingSection(
                    amount = state.sendingAmount,
                    currency = state.sendingCurrency,
                    isWarningVisible = state.sendingLimitExceededMessage != null,
                    onAmountChange = {
                        onAction(CurrencyExchangeScreenAction.OnSendingAmountInputChange(it))
                    },
                    onCurrencyClick = {
                        onAction(
                            CurrencyExchangeScreenAction.SelectCurrencyClicked(
                                currencyInputType = CurrencyInputType.SENDING
                            )
                        )
                    }
                )

                ReceivingSection(
                    amount = state.receivingAmount,
                    currency = state.receivingCurrency,
                    onAmountChange = {
                        onAction(CurrencyExchangeScreenAction.OnReceivingAmountInputChange(it))
                    },
                    onCurrencyClick = {
                        onAction(
                            CurrencyExchangeScreenAction.SelectCurrencyClicked(
                                currencyInputType = CurrencyInputType.RECEIVING
                            )
                        )
                    }
                )
            }

            // Swap icon
            Icon(
                painter = painterResource(id = R.drawable.ic_swap),
                contentDescription = stringResource(R.string.swap_currencies),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
                    .clickable { onAction(CurrencyExchangeScreenAction.SwapClicked) }
                    .align(BiasAlignment(horizontalBias = -0.75f, verticalBias = 0f))
            )

            state.exchangeRatio?.let { ratio ->
                ExchangeRateBadge(
                    ratioText = stringResource(
                        R.string.exchange_rate_format,
                        state.sendingCurrency.code,
                        ratio,
                        state.receivingCurrency.code
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        state.sendingLimitExceededMessage?.let { warningMessage ->
            WarningMessage(message = warningMessage)
        }
    }
}

@Composable
fun SendingSection(
    amount: String,
    currency: Currency,
    isWarningVisible: Boolean,
    onAmountChange: (String) -> Unit,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountColor = if (isWarningVisible) errorColor else primaryColor
    val outlineColor = if (isWarningVisible) errorColor else Color.Transparent

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 2.dp,
            color = outlineColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        CurrencyInputSection(
            label = stringResource(R.string.sending_from),
            amount = amount,
            currency = currency,
            onAmountChange = onAmountChange,
            onCurrencyClick = onCurrencyClick,
            amountColor = amountColor,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ReceivingSection(
    amount: String,
    currency: Currency,
    onAmountChange: (String) -> Unit,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CurrencyInputSection(
        label = stringResource(R.string.receiver_gets),
        amount = amount,
        currency = currency,
        onAmountChange = onAmountChange,
        onCurrencyClick = onCurrencyClick,
        amountColor = Color.Black,
        modifier = modifier.padding(16.dp)
    )
}

@Composable
fun CurrencyInputSection(
    label: String,
    amount: String,
    currency: Currency,
    onAmountChange: (String) -> Unit,
    onCurrencyClick: () -> Unit,
    amountColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = secondaryTextColor
            )

            CurrencySelector(
                currency = currency,
                onCurrencyClick = onCurrencyClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            DecimalTextField(
                value = amount,
                onValueChange = onAmountChange,
                textColor = amountColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DecimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val decimalNumberRegex = Regex("^\\d*\\.?\\d{0,2}$")

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty()) {
                onValueChange(newValue)
            } else if (newValue.matches(decimalNumberRegex)) {
                onValueChange(newValue)
            }
        },
        modifier = modifier,
        textStyle = TextStyle(
            color = textColor,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        singleLine = true,
        cursorBrush = SolidColor(textColor),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.default_amount),
                        style = TextStyle(
                            color = textColor.copy(alpha = 0.3f),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun CurrencySelector(
    currency: Currency,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onCurrencyClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(CurrencyDefaults.getBigIconForCurrency(currency)),
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.size(8.dp))
        Text(
            text = currency.code,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.size(4.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(R.string.select_currency),
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ExchangeRateBadge(
    ratioText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black
    ) {
        Text(
            text = ratioText,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
        )
    }
}

@Composable
fun WarningMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(warningSurfaceColor)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = stringResource(R.string.warning),
            tint = warningTextColor,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = warningTextColor
        )
    }
}

@Composable
fun ErrorPanel(
    state: ErrorPanelState?,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        state?.isVisible ?: false,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(
                durationMillis = 350,
                delayMillis = 0,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 350,
                delayMillis = 0,
                easing = LinearEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(
                durationMillis = 250,
                delayMillis = 0,
                easing = FastOutLinearInEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 250,
                delayMillis = 0,
                easing = LinearEasing
            )
        )
    ) {
        state?.let {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_error),
                        contentDescription = stringResource(R.string.error),
                        tint = Color.Unspecified
                    )

                    Spacer(Modifier.size(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(state.titleResId),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = stringResource(state.messageResId),
                            fontSize = 16.sp,
                            color = secondaryTextColor
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = onDismissClick,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.dismiss_error),
                            tint = secondaryTextColor,
                        )
                    }
                }
            }
        }
    }
}