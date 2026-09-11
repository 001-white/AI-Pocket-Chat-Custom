```kotlin
package com.situ.aichat.ui.liuli.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.situ.aichat.R
import com.situ.aichat.data.model.ApiProviderType
import com.situ.aichat.data.repository.isHttpsBaseUrl
import com.situ.aichat.ui.liuli.designsystem.LiuliButton
import com.situ.aichat.ui.liuli.designsystem.LiuliButtonStyle
import com.situ.aichat.ui.liuli.designsystem.LiuliMenuEntry
import com.situ.aichat.ui.liuli.page.LiuliGroup
import com.situ.aichat.ui.liuli.page.LiuliInputRow
import com.situ.aichat.ui.liuli.page.LiuliMenuRow
import com.situ.aichat.ui.liuli.page.LiuliPageGeometry
import com.situ.aichat.ui.liuli.page.LiuliRowBase
import com.situ.aichat.ui.settings.ModelCatalogUiState

/** 三处硬编码中文（与暖陶 `ApiConfigScreen.kt` 同值·A-6）。 */
private const val API_NAME_LABEL = "API 名称"
private const val PROVIDER_LABEL = "服务商"
private const val BASE_URL_LABEL = "Base URL"
private const val SAVE_AND_ACTIVATE_LABEL = "保存并启用"

/**
 * 新建配置表单（琉璃·图纸 2026-09-06 卷五 §4.1 屏 7 的第一张卡）。
 *
 * 增加用户自定义 API 名称字段，并通过 [onDisplayNameChange] 向外层传递。
 *
 * 字段顺序：
 * API 名称 → 服务商 → Base URL → 模型 → API Key → 保存并启用。
 *
 * 原有服务商切换、Base URL 校验、模型目录、API Key 以及保存条件均保持不变。
 */
@Composable
internal fun LiuliApiConfigForm(
    displayName: String,
    provider: ApiProviderType,
    baseUrl: String,
    model: String,
    apiKey: String,
    catalogState: ModelCatalogUiState,
    providerMenuOpen: Boolean,
    onProviderMenuOpenChange: (Boolean) -> Unit,
    onProviderChange: (ApiProviderType) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onFetchModels: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val urlInsecure = baseUrl.isNotBlank() && !isHttpsBaseUrl(baseUrl)
    val canSave = apiKey.isNotBlank() &&
        baseUrl.isNotBlank() &&
        model.isNotBlank() &&
        isHttpsBaseUrl(baseUrl)

    LiuliGroup(
        modifier = modifier,
        footer = liuliKnownCapabilityHint(model),
    ) {
        LiuliInputRow(
            label = API_NAME_LABEL,
            value = displayName,
            onValueChange = onDisplayNameChange,
        )

        LiuliMenuRow(
            title = PROVIDER_LABEL,
            value = provider.displayName,
            options = ApiProviderType.entries.map { p ->
                LiuliMenuEntry(
                    text = p.displayName,
                    selected = provider == p,
                    onClick = { onProviderChange(p) },
                )
            },
            expanded = providerMenuOpen,
            onExpandedChange = onProviderMenuOpenChange,
            divider = false,
        )

        LiuliInputRow(
            label = BASE_URL_LABEL,
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            supportingText = if (urlInsecure) {
                stringResource(R.string.api_url_https_required)
            } else {
                null
            },
        )

        LiuliCatalogField(
            value = model,
            onValueChange = onModelChange,
            label = LiuliApiText.MODEL_LABEL,
            items = catalogState.models.map {
                it.id to (it.subtitle?.let { s -> "${it.name} · $s" } ?: it.name)
            },
            loading = catalogState.isLoading,
            error = catalogState.error,
            onFetch = onFetchModels,
            emptyHint = stringResource(R.string.api_model_no_match),
            // 拉到 0 条（`Empty`·与从没拉过明确区分）
            // → 中性提示「可直接输入模型名」（暖陶 :597–601·复核 R1 A2）。
            fetchedEmptyHint = if (catalogState is ModelCatalogUiState.Empty) {
                stringResource(R.string.api_models_empty_hint)
            } else {
                null
            },
        )

        LiuliApiKeyRow(
            value = apiKey,
            onValueChange = onApiKeyChange,
        )

        LiuliRowBase(verticalPadding = LiuliPageGeometry.rowTwoLinePad) {
            LiuliButton(
                onClick = onSave,
                style = LiuliButtonStyle.Prominent,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(SAVE_AND_ACTIVATE_LABEL)
            }
        }
    }
}
```
